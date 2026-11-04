package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.FeedbackDto;
import learning_management_system.backend.dto.GradeSubmissionRequest;
import learning_management_system.backend.dto.StudentSubmissionSummaryDto;
import learning_management_system.backend.dto.SubmissionDto;
import learning_management_system.backend.dto.SubmitAssignmentRequest;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Feedback;
import learning_management_system.backend.entity.GroupMember;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.AssignmentType;
import learning_management_system.backend.enums.DefaultPostPolicy;
import learning_management_system.backend.enums.LatePolicyStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.AssignmentMapper;
import learning_management_system.backend.repository.AssignmentRepository;
import learning_management_system.backend.repository.AssignmentSubmissionRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.FeedbackRepository;
import learning_management_system.backend.repository.GroupMemberRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.AssignmentService;
import learning_management_system.backend.service.AssignmentService.Schedule;
import learning_management_system.backend.service.StudentModuleProgressService;
import learning_management_system.backend.service.SubmissionService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubmissionServiceImplementation implements SubmissionService {
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final FeedbackRepository feedback;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final GroupMemberRepository members;
    private final AssignmentService assignmentService;
    private final StudentModuleProgressService progress;
    private final UserService userService;
    private final CourseAccess access;
    private final AssignmentMapper assignmentMapper;

    public SubmissionServiceImplementation(AssignmentRepository assignments, AssignmentSubmissionRepository submissions,
                                           FeedbackRepository feedback, CourseRepository courses,
                                           EnrollmentRepository enrollments, GroupMemberRepository members,
                                           AssignmentService assignmentService, StudentModuleProgressService progress,
                                           UserService userService, CourseAccess access, AssignmentMapper assignmentMapper) {
        this.assignments = assignments;
        this.submissions = submissions;
        this.feedback = feedback;
        this.courses = courses;
        this.enrollments = enrollments;
        this.members = members;
        this.assignmentService = assignmentService;
        this.progress = progress;
        this.userService = userService;
        this.access = access;
        this.assignmentMapper = assignmentMapper;
    }

    // ---------------------------------------------------------------- students

    @Transactional
    @Override
    public SubmissionDto submit(UUID assignmentId, SubmitAssignmentRequest r) {
        Assignment a = load(assignmentId);
        Course c = course(a.getCourseId());
        access.requireCourseViewer(c);
        UUID me = Access.ctx().userId();
        if (!progress.isTrackedStudent(c.getCourseId(), me)) throw ApiException.forbidden("Only students of the course can submit");
        String lock = assignmentService.submitLockReason(a, me);
        if (lock != null) throw ApiException.conflict("SUBMISSION_CLOSED", lock);
        boolean draft = Boolean.TRUE.equals(r.draft());
        String text = blankToNull(r.submissionText());
        String url = blankToNull(r.submissionUrl());
        if (AssignmentServiceImplementation.TEXT_TYPES.contains(a.getAssignmentType())) {
            if (text == null && !draft) throw ApiException.badRequest("TEXT_REQUIRED", "This assignment takes a text submission");
            url = null;
        } else if (a.getAssignmentType() == AssignmentType.URL_SUBMISSION) {
            if (url == null && !draft) throw ApiException.badRequest("URL_REQUIRED", "This assignment takes a link");
            if (url != null && !url.matches("(?i)^https?://\\S+$")) throw ApiException.badRequest("INVALID_URL", "The link must start with http:// or https://");
            text = null;
        }

        UUID groupId = null;
        List<UUID> groupMates = List.of();
        if (Boolean.TRUE.equals(a.getAllowGroupSubmission()) && a.getGroupSetId() != null) {
            GroupMember gm = members.activeInSet(a.getGroupSetId(), me)
                    .orElseThrow(() -> ApiException.badRequest("NOT_IN_GROUP", "Join a group of this assignment's group set before submitting"));
            groupId = gm.getGroupId();
            if (!draft) {
                groupMates = members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(groupId).stream().map(GroupMember::getUserId)
                        .filter(u -> !u.equals(me) && progress.isTrackedStudent(c.getCourseId(), u)).toList();
            }
        }
        AssignmentSubmission s = write(a, me, text, url, r.submissionNotes(), draft, groupId);
        for (UUID mate : groupMates) write(a, mate, text, url, r.submissionNotes(), false, groupId);
        if (!draft) {
            progress.evaluate(c.getCourseId(), me);
            for (UUID mate : groupMates) progress.evaluate(c.getCourseId(), mate);
        }
        return toDto(s, false, a);
    }

    /** Saves a draft or hands in an attempt for one student (reusing their open draft). */
    private AssignmentSubmission write(Assignment a, UUID userId, String text, String url, String notes, boolean draft, UUID groupId) {
        List<AssignmentSubmission> mine = submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(a.getAssignmentId(), userId);
        AssignmentSubmission s = mine.stream().filter(x -> x.getSubmissionStatus() == AssignmentSubmissionStatus.DRAFT).reduce((x, y) -> y).orElse(null);
        if (s == null) {
            s = new AssignmentSubmission();
            s.setAssignmentId(a.getAssignmentId());
            s.setUserId(userId);
            s.setSubmissionNumber(submissions.maxSubmissionNumber(a.getAssignmentId(), userId) + 1);
        }
        s.setSubmissionText(text);
        s.setSubmissionUrl(url);
        s.setSubmissionNotes(notes);
        s.setWordCount(text == null ? null : wordCount(text));
        s.setCharacterCount(text == null ? null : plain(text).length());
        s.setIsGroupSubmission(groupId != null);
        s.setGroupId(groupId);
        s.setSubmissionTypeUsed(url != null ? "ONLINE_URL" : "ONLINE_TEXT");
        if (draft) {
            s.setSubmissionStatus(AssignmentSubmissionStatus.DRAFT);
        } else {
            OffsetDateTime now = OffsetDateTime.now();
            Schedule sch = assignmentService.schedule(a, userId);
            boolean late = sch.due() != null && now.isAfter(sch.due());
            s.setSubmissionStatus(AssignmentSubmissionStatus.SUBMITTED);
            s.setSubmittedAt(now);
            s.setSubmittedLate(late);
            s.setLatePolicyStatus(late ? LatePolicyStatus.LATE : LatePolicyStatus.NONE);
        }
        return submissions.saveAndFlush(s);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SubmissionDto> mine(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireCourseViewer(course(a.getCourseId()));
        UUID me = Access.ctx().userId();
        return submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, me).stream()
                .map(s -> toDto(s, false, a)).toList();
    }

    // ---------------------------------------------------------------- graders

    @Transactional(readOnly = true)
    @Override
    public List<StudentSubmissionSummaryDto> forGrading(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireGrader(a.getCourseId());
        Map<UUID, Boolean> students = new LinkedHashMap<>();
        for (Enrollment e : enrollments.activeInCourseOfTypes(a.getCourseId(), EnrollmentProvisioningServiceImplementation.SEAT_TYPES)) {
            students.put(e.getUserId(), true);
        }
        Map<UUID, List<AssignmentSubmission>> byUser = new HashMap<>();
        for (AssignmentSubmission s : submissions.findByAssignmentIdOrderByUserIdAscSubmissionNumberAsc(assignmentId)) {
            if (s.getSubmissionStatus() == AssignmentSubmissionStatus.DRAFT) continue;            // drafts are private
            byUser.computeIfAbsent(s.getUserId(), k -> new ArrayList<>()).add(s);
        }
        boolean anonymous = Boolean.TRUE.equals(a.getIsAnonymousGrading());
        Map<UUID, UserSummaryDto> names = anonymous ? Map.of() : userService.summariesById(students.keySet());
        OffsetDateTime now = OffsetDateTime.now();
        List<StudentSubmissionSummaryDto> out = new ArrayList<>();
        for (UUID u : students.keySet()) {
            List<AssignmentSubmission> list = byUser.getOrDefault(u, List.of());
            AssignmentSubmission latest = list.isEmpty() ? null : list.get(list.size() - 1);
            Schedule sch = assignmentService.schedule(a, u);
            boolean excused = list.stream().anyMatch(s -> Boolean.TRUE.equals(s.getIsExcused()));
            boolean handedIn = list.stream().anyMatch(s -> s.getSubmittedAt() != null);
            boolean missing = !handedIn && !excused && sch.due() != null && now.isAfter(sch.due())
                    && AssignmentServiceImplementation.ONLINE_TYPES.contains(a.getAssignmentType());
            LatePolicyStatus lp = missing ? LatePolicyStatus.MISSING
                    : latest != null && latest.getLatePolicyStatus() != null ? latest.getLatePolicyStatus() : LatePolicyStatus.NONE;
            UserSummaryDto n = names.get(u);
            int attempts = (int) list.stream().filter(s -> s.getSubmittedAt() != null).count();
            out.add(new StudentSubmissionSummaryDto(u, n == null ? null : n.username(), n == null ? null : n.displayName(), attempts,
                    latest == null ? null : toDto(latest, true, a), lp, missing, sch.due()));
        }
        out.sort(Comparator.comparing(x -> x.username() == null ? x.userId().toString() : x.username()));
        return out;
    }

    @Transactional(readOnly = true)
    @Override
    public SubmissionDto get(UUID submissionId) {
        AssignmentSubmission s = submissions.findById(submissionId).orElseThrow(() -> ApiException.notFound("Submission"));
        Assignment a = load(s.getAssignmentId());
        if (Access.isSelf(s.getUserId())) {
            access.requireCourseViewer(course(a.getCourseId()));
            return toDto(s, false, a);
        }
        access.requireGrader(a.getCourseId());
        if (s.getSubmissionStatus() == AssignmentSubmissionStatus.DRAFT) throw ApiException.notFound("Submission");
        return toDto(s, true, a);
    }

    @Transactional
    @Override
    public SubmissionDto grade(UUID submissionId, GradeSubmissionRequest r) {
        AssignmentSubmission s = submissions.findById(submissionId).orElseThrow(() -> ApiException.notFound("Submission"));
        Assignment a = load(s.getAssignmentId());
        access.requireGrader(a.getCourseId());
        if (s.getSubmissionStatus() == AssignmentSubmissionStatus.DRAFT) throw ApiException.conflict("NOT_SUBMITTED", "Drafts cannot be graded");
        if (Access.isSelf(s.getUserId())) throw ApiException.forbidden("You cannot grade your own work");
        Course c = course(a.getCourseId());
        List<AssignmentSubmission> targets = new ArrayList<>(List.of(s));
        if (Boolean.TRUE.equals(s.getIsGroupSubmission()) && s.getGroupId() != null && !Boolean.TRUE.equals(a.getGradeGroupIndividually())) {
            for (GroupMember gm : members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(s.getGroupId())) {
                if (gm.getUserId().equals(s.getUserId())) continue;
                submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(a.getAssignmentId(), gm.getUserId()).stream()
                        .filter(x -> s.getGroupId().equals(x.getGroupId()) && x.getSubmittedAt() != null)
                        .reduce((x, y) -> y).ifPresent(targets::add);
            }
        }
        UUID me = Access.ctx().userId();
        boolean automatic = c.getDefaultPostPolicy() != DefaultPostPolicy.MANUAL;
        for (AssignmentSubmission t : targets) {
            Feedback f = feedback.findFirstBySubmissionIdAndEvaluatorId(t.getSubmissionId(), me).orElseGet(Feedback::new);
            f.setSubmissionId(t.getSubmissionId());
            f.setEvaluatorId(me);
            f.setGradeEarned(r.gradeEarned());
            f.setGradePercentage(percentage(r.gradeEarned(), a.getPointsPossible()));
            f.setGradeLetter(r.gradeLetter());
            f.setFeedbackText(r.feedbackText());
            f.setIsPassed(r.isPassed());
            f.setDateGraded(OffsetDateTime.now());
            feedback.saveAndFlush(f);
            boolean alreadyPosted = t.getSubmissionStatus() == AssignmentSubmissionStatus.RETURNED;
            t.setSubmissionStatus(automatic || alreadyPosted ? AssignmentSubmissionStatus.RETURNED : AssignmentSubmissionStatus.GRADED);
            submissions.saveAndFlush(t);
            if (t.getSubmissionStatus() == AssignmentSubmissionStatus.RETURNED) progress.evaluate(c.getCourseId(), t.getUserId());
        }
        return toDto(s, true, a);
    }

    @Transactional
    @Override
    public int postGrades(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireGrader(a.getCourseId());
        return submissions.postGrades(assignmentId);
    }

    @Transactional
    @Override
    public SubmissionDto setExcused(UUID assignmentId, UUID userId, boolean excused) {
        Assignment a = load(assignmentId);
        access.requireGrader(a.getCourseId());
        if (!progress.isTrackedStudent(a.getCourseId(), userId)) throw ApiException.badRequest("NOT_A_STUDENT", "The user is not a student of this course");
        List<AssignmentSubmission> mine = submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, userId);
        AssignmentSubmission s = mine.stream().filter(x -> x.getSubmissionStatus() != AssignmentSubmissionStatus.DRAFT).reduce((x, y) -> y).orElse(null);
        if (s == null) {
            if (!excused) throw ApiException.notFound("Submission");
            s = new AssignmentSubmission();                 // an excused student needs a row even without work
            s.setAssignmentId(assignmentId);
            s.setUserId(userId);
            s.setSubmissionNumber(submissions.maxSubmissionNumber(assignmentId, userId) + 1);
            s.setSubmissionStatus(AssignmentSubmissionStatus.RETURNED);
        }
        s.setIsExcused(excused);
        submissions.saveAndFlush(s);
        progress.evaluate(a.getCourseId(), userId);
        return toDto(s, true, a);
    }

    @Transactional
    @Override
    public SubmissionDto reassign(UUID submissionId) {
        AssignmentSubmission s = submissions.findById(submissionId).orElseThrow(() -> ApiException.notFound("Submission"));
        Assignment a = load(s.getAssignmentId());
        access.requireGrader(a.getCourseId());
        if (s.getSubmittedAt() == null) throw ApiException.conflict("NOT_SUBMITTED", "Only handed-in work can be reassigned");
        s.setSubmissionStatus(AssignmentSubmissionStatus.REASSIGNED);
        submissions.saveAndFlush(s);
        progress.evaluate(a.getCourseId(), s.getUserId());
        return toDto(s, true, a);
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Graders see the latest feedback and (unless grading is anonymous) the student's name; students see feedback only
     * once the grade is posted (RETURNED).
     */
    private SubmissionDto toDto(AssignmentSubmission s, boolean asGrader, Assignment a) {
        boolean showFeedback = asGrader || s.getSubmissionStatus() == AssignmentSubmissionStatus.RETURNED;
        FeedbackDto f = showFeedback ? feedback.findFirstBySubmissionIdOrderByDateGradedDesc(s.getSubmissionId()).map(assignmentMapper::toDto).orElse(null) : null;
        String username = null, displayName = null;
        if (!asGrader || !Boolean.TRUE.equals(a.getIsAnonymousGrading())) {
            UserSummaryDto u = userService.summariesById(List.of(s.getUserId())).get(s.getUserId());
            if (u != null) { username = u.username(); displayName = u.displayName(); }
        }
        return assignmentMapper.toDto(s, username, displayName, f);
    }

    private static BigDecimal percentage(BigDecimal earned, BigDecimal possible) {
        if (earned == null || possible == null || possible.signum() <= 0) return null;
        BigDecimal p = earned.multiply(BigDecimal.valueOf(100)).divide(possible, 2, RoundingMode.HALF_UP);
        return p.min(BigDecimal.valueOf(100));            // extra credit keeps its points; the percentage column stops at 100
    }

    private static String plain(String html) {
        return html.replaceAll("<[^>]*>", " ").replaceAll("&nbsp;", " ").replaceAll("\\s+", " ").trim();
    }

    private static int wordCount(String html) {
        String p = plain(html);
        return p.isEmpty() ? 0 : p.split(" ").length;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private Assignment load(UUID id) {
        return assignments.findById(id).orElseThrow(() -> ApiException.notFound("Assignment"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
