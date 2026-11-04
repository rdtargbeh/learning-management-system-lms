package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.service.ItemAvailabilityService;
import learning_management_system.backend.repository.DifferentiationTagRepository;
import learning_management_system.backend.repository.DifferentiationTagMemberRepository;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.BlueprintLockAspect;
import static learning_management_system.backend.enums.BlueprintLockAspect.*;
import learning_management_system.backend.dto.AssignmentDto;
import learning_management_system.backend.dto.AssignmentOverrideDto;
import learning_management_system.backend.dto.AssignmentViewDto;
import learning_management_system.backend.dto.CreateAssignmentOverrideRequest;
import learning_management_system.backend.dto.CreateAssignmentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.StudentAssignmentStatusDto;
import learning_management_system.backend.dto.UpdateAssignmentRequest;
import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentOverride;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Feedback;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;
import learning_management_system.backend.enums.AssignmentGradingType;
import learning_management_system.backend.enums.AssignmentStatus;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.AssignmentType;
import learning_management_system.backend.enums.LatePolicyStatus;
import learning_management_system.backend.enums.SubmissionType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.AssignmentMapper;
import learning_management_system.backend.repository.AssignmentOverrideRepository;
import learning_management_system.backend.repository.AssignmentRepository;
import learning_management_system.backend.repository.AssignmentSubmissionRepository;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.CourseModuleRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.FeedbackRepository;
import learning_management_system.backend.repository.GroupMemberRepository;
import learning_management_system.backend.repository.GroupSetRepository;
import learning_management_system.backend.repository.ModuleCompletionRequirementRepository;
import learning_management_system.backend.repository.StudentGroupRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.AssignmentService;
import learning_management_system.backend.service.StudentModuleProgressService;
import learning_management_system.backend.service.StudentModuleProgressService.ModuleState;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImplementation implements AssignmentService {
    /** Types students hand in through this API (text or a link). Files, media and tools come later. */
    static final Set<AssignmentType> TEXT_TYPES = EnumSet.of(AssignmentType.TEXT_ENTRY, AssignmentType.ESSAY, AssignmentType.CODE_SUBMISSION);
    static final Set<AssignmentType> ONLINE_TYPES = EnumSet.of(AssignmentType.TEXT_ENTRY, AssignmentType.ESSAY,
            AssignmentType.CODE_SUBMISSION, AssignmentType.URL_SUBMISSION);

    private final AssignmentRepository assignments;
    private final AssignmentOverrideRepository overrides;
    private final AssignmentSubmissionRepository submissions;
    private final FeedbackRepository feedback;
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final CourseSectionRepository sections;
    private final ContentRepository contents;
    private final EnrollmentRepository enrollments;
    private final GroupSetRepository groupSets;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository members;
    private final ModuleCompletionRequirementRepository requirements;
    private final StudentModuleProgressService progress;
    private final CourseAccess access;
    private final AssignmentMapper assignmentMapper;
    private final EntityRefresher refresher;

    private final BlueprintContentLockService blueprintLocks;

    private final DifferentiationTagRepository tags;
    private final DifferentiationTagMemberRepository tagMembers;

    private final ItemAvailabilityService availability;

    private final learning_management_system.backend.service.CoursePaceService paces;

    public AssignmentServiceImplementation(AssignmentRepository assignments, AssignmentOverrideRepository overrides,
                                           AssignmentSubmissionRepository submissions, FeedbackRepository feedback,
                                           CourseRepository courses, CourseModuleRepository modules,
                                           CourseSectionRepository sections, ContentRepository contents,
                                           EnrollmentRepository enrollments, GroupSetRepository groupSets,
                                           StudentGroupRepository groups, GroupMemberRepository members,
                                           ModuleCompletionRequirementRepository requirements,
                                           StudentModuleProgressService progress, CourseAccess access,
                                           AssignmentMapper assignmentMapper, EntityRefresher refresher,
            BlueprintContentLockService blueprintLocks,
            DifferentiationTagRepository tags, DifferentiationTagMemberRepository tagMembers,
            ItemAvailabilityService availability,
            learning_management_system.backend.service.CoursePaceService paces) {
        this.paces = paces;
        this.availability = availability;
        this.tags = tags;
        this.tagMembers = tagMembers;
        this.blueprintLocks = blueprintLocks;
        this.assignments = assignments;
        this.overrides = overrides;
        this.submissions = submissions;
        this.feedback = feedback;
        this.courses = courses;
        this.modules = modules;
        this.sections = sections;
        this.contents = contents;
        this.enrollments = enrollments;
        this.groupSets = groupSets;
        this.groups = groups;
        this.members = members;
        this.requirements = requirements;
        this.progress = progress;
        this.access = access;
        this.assignmentMapper = assignmentMapper;
        this.refresher = refresher;
    }

    // ---------------------------------------------------------------- reading

    @Transactional
    @Override
    public List<AssignmentViewDto> list(UUID courseId) {
        Course c = course(courseId);
        access.requireCourseViewer(c);
        List<Assignment> all = assignments.findByCourseIdOrderByDateDueAscAssignmentTitleAsc(courseId);
        if (isStaff(courseId)) return all.stream().map(a -> new AssignmentViewDto(assignmentMapper.toDto(a), null)).toList();
        UUID me = Access.ctx().userId();
        Map<UUID, ModuleState> states = progress.evaluate(courseId, me);
        List<AssignmentViewDto> out = new ArrayList<>();
        for (Assignment a : all) {
            if (!visibleToStudent(a, states)) continue;
            out.add(new AssignmentViewDto(assignmentMapper.toDto(a), status(a, me)));
        }
        return out;
    }

    @Transactional
    @Override
    public AssignmentViewDto get(UUID assignmentId) {
        Assignment a = load(assignmentId);
        Course c = course(a.getCourseId());
        access.requireCourseViewer(c);
        if (isStaff(c.getCourseId())) return new AssignmentViewDto(assignmentMapper.toDto(a), null);
        UUID me = Access.ctx().userId();
        if (!visibleToStudent(a, progress.evaluate(c.getCourseId(), me))) throw ApiException.notFound("Assignment");
        return new AssignmentViewDto(assignmentMapper.toDto(a), status(a, me));
    }

    private static boolean visibleToStudent(Assignment a, Map<UUID, ModuleState> states) {
        return Boolean.TRUE.equals(a.getIsPublished()) && (a.getModuleId() == null || states.containsKey(a.getModuleId()));
    }

    /** The student's status card. Grades show only once posted (RETURNED). */
    private StudentAssignmentStatusDto status(Assignment a, UUID me) {
        Schedule sch = schedule(a, me);
        List<AssignmentSubmission> mine = submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(a.getAssignmentId(), me);
        AssignmentSubmission latest = mine.isEmpty() ? null : mine.get(mine.size() - 1);
        boolean excused = mine.stream().anyMatch(s -> Boolean.TRUE.equals(s.getIsExcused()));
        boolean handedIn = mine.stream().anyMatch(s -> s.getSubmittedAt() != null && StudentModuleProgressServiceImplementation.HANDED_IN.contains(s.getSubmissionStatus()));
        LatePolicyStatus late = latest != null && latest.getLatePolicyStatus() != null ? latest.getLatePolicyStatus() : LatePolicyStatus.NONE;
        if (!handedIn && !excused && sch.due() != null && OffsetDateTime.now().isAfter(sch.due()) && ONLINE_TYPES.contains(a.getAssignmentType())) {
            late = LatePolicyStatus.MISSING;
        }
        Feedback posted = null;
        for (int i = mine.size() - 1; i >= 0 && posted == null; i--) {
            if (mine.get(i).getSubmissionStatus() == AssignmentSubmissionStatus.RETURNED) {
                posted = feedback.findFirstBySubmissionIdOrderByDateGradedDesc(mine.get(i).getSubmissionId()).orElse(null);
            }
        }
        String lock = submitLockReason(a, me);
        return new StudentAssignmentStatusDto(sch.available(), sch.due(), sch.close(), lock == null, lock, attemptsUsed(a.getAssignmentId(), me),
                sch.maxAttempts(), latest == null ? null : latest.getSubmissionStatus(), late, excused,
                posted == null ? null : posted.getGradeEarned(), posted == null ? null : posted.getGradeLetter());
    }

    // ---------------------------------------------------------------- authoring

    @Transactional
    @Override
    public AssignmentDto create(UUID courseId, CreateAssignmentRequest r) {
        Course c = course(courseId);
        access.requireCourseEditor(courseId);
        if (Boolean.TRUE.equals(c.getIsArchived())) throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
        Assignment a = assignmentMapper.toEntity(r);
        a.setCourseId(courseId);
        a.setAssignmentTitle(r.assignmentTitle().trim());
        a.setSubmissionType(submissionTypeFor(r.assignmentType()));
        a.setGradingType(r.gradingType() == null ? AssignmentGradingType.POINTS : r.gradingType());
        a.setIsPublished(false);
        a.setAssignmentStatus(AssignmentStatus.DRAFT);
        if (r.moduleId() != null) {
            module(r.moduleId(), courseId);
            a.setDisplayOrder(nextOrder(r.moduleId()));
        }
        if (r.groupSetId() != null) {
            GroupSet gs = groupSets.findById(r.groupSetId()).filter(x -> x.getCourseId().equals(courseId))
                    .orElseThrow(() -> ApiException.badRequest("GROUP_SET_NOT_IN_COURSE", "The group set does not belong to this course"));
            a.setGroupSetId(gs.getGroupSetId());
            a.setAllowGroupSubmission(true);
        } else {
            a.setAllowGroupSubmission(false);
        }
        if (Boolean.TRUE.equals(r.peerReviewRequired()) && r.peerReviewCount() == null) a.setPeerReviewCount(1);
        validateDates(a);
        return assignmentMapper.toDto(refresher.refresh(assignments.saveAndFlush(a)));
    }

    @Transactional
    @Override
    public AssignmentDto update(UUID assignmentId, UpdateAssignmentRequest r) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        java.util.EnumSet<BlueprintLockAspect> touched = java.util.EnumSet.noneOf(BlueprintLockAspect.class);
        if (r.assignmentTitle() != null || r.assignmentDescription() != null || r.assignmentInstructions() != null) touched.add(CONTENT);
        if (r.pointsPossible() != null || r.gradingType() != null || r.omitFromFinalGrade() != null) touched.add(POINTS);
        if (r.dateDue() != null || r.dateClose() != null) touched.add(DUE_DATES);
        if (r.dateAvailable() != null) touched.add(AVAILABILITY);
        if (r.assignmentType() != null || r.maxSubmissions() != null || r.lateSubmissionAllowed() != null || r.lateSubmissionPenaltyPercent() != null
                || r.isAnonymousGrading() != null || r.gradeGroupIndividually() != null || r.peerReviewRequired() != null || r.peerReviewCount() != null) {
            touched.add(SETTINGS);
        }
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, touched);
        assignmentMapper.updateEntity(r, a);
        if (r.assignmentType() != null) a.setSubmissionType(submissionTypeFor(r.assignmentType()));
        if (Boolean.TRUE.equals(a.getPeerReviewRequired()) && a.getPeerReviewCount() == null) a.setPeerReviewCount(1);
        validateDates(a);
        return assignmentMapper.toDto(assignments.saveAndFlush(a));
    }

    @Transactional
    @Override
    public AssignmentDto setPublished(UUID assignmentId, boolean published) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, java.util.EnumSet.of(AVAILABILITY));
        if (!published && hasStudentWork(assignmentId)) {
            throw ApiException.conflict("HAS_SUBMISSIONS", "Students have already submitted; the assignment cannot be unpublished");
        }
        a.setIsPublished(published);
        a.setAssignmentStatus(published ? AssignmentStatus.PUBLISHED : AssignmentStatus.DRAFT);
        return assignmentMapper.toDto(assignments.saveAndFlush(a));
    }

    @Transactional
    @Override
    public AssignmentDto move(UUID assignmentId, MoveContentRequest r) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, java.util.EnumSet.of(SETTINGS));
        if (r.moduleId() == null) {
            requirements.deleteForAssignmentOutsideModule(assignmentId, null);
            a.setModuleId(null);
            a.setDisplayOrder(0);
        } else if (!r.moduleId().equals(a.getModuleId())) {
            module(r.moduleId(), a.getCourseId());
            requirements.deleteForAssignmentOutsideModule(assignmentId, r.moduleId());
            a.setModuleId(r.moduleId());
            a.setDisplayOrder(nextOrder(r.moduleId()));
        }
        return assignmentMapper.toDto(assignments.saveAndFlush(a));
    }

    @Transactional
    @Override
    public void delete(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, java.util.EnumSet.of(CONTENT));
        if (hasStudentWork(assignmentId)) {
            throw ApiException.conflict("HAS_SUBMISSIONS", "Students have already submitted; the assignment cannot be deleted");
        }
        assignments.delete(a);
        assignments.flush();
    }

    // ---------------------------------------------------------------- overrides

    @Transactional(readOnly = true)
    @Override
    public List<AssignmentOverrideDto> overrides(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        return overrides.findByAssignmentIdOrderByDateCreatedAsc(assignmentId).stream().map(assignmentMapper::toDto).toList();
    }

    @Transactional
    @Override
    public AssignmentOverrideDto addOverride(UUID assignmentId, CreateAssignmentOverrideRequest r) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, java.util.EnumSet.of(DUE_DATES));
        switch (r.targetType()) {
            case "STUDENT" -> {
                if (r.targetUserId() == null) throw ApiException.badRequest("TARGET_REQUIRED", "STUDENT overrides need targetUserId");
                if (!progress.isTrackedStudent(a.getCourseId(), r.targetUserId())) {
                    throw ApiException.badRequest("NOT_A_STUDENT", "The user is not a student of this course");
                }
            }
            case "SECTION" -> {
                if (r.targetSectionId() == null) throw ApiException.badRequest("TARGET_REQUIRED", "SECTION overrides need targetSectionId");
                sections.findById(r.targetSectionId()).filter(s -> s.getCourseId().equals(a.getCourseId()))
                        .orElseThrow(() -> ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course"));
            }
            case "TAG" -> {
                if (r.targetTagId() == null) throw ApiException.badRequest("TARGET_REQUIRED", "TAG overrides need targetTagId");
                tags.findById(r.targetTagId()).filter(t -> t.getCourseId().equals(a.getCourseId()))
                        .orElseThrow(() -> ApiException.badRequest("TAG_NOT_IN_COURSE", "The tag does not belong to this course"));
            }
            default -> {
                if (r.targetGroupId() == null) throw ApiException.badRequest("TARGET_REQUIRED", "GROUP overrides need targetGroupId");
                StudentGroup g = groups.findById(r.targetGroupId()).orElseThrow(() -> ApiException.badRequest("UNKNOWN_GROUP", "Unknown group"));
                groupSets.findById(g.getGroupSetId()).filter(gs -> gs.getCourseId().equals(a.getCourseId()))
                        .orElseThrow(() -> ApiException.badRequest("GROUP_NOT_IN_COURSE", "The group does not belong to this course"));
            }
        }
        AssignmentOverride o = assignmentMapper.toEntity(r);
        o.setAssignmentId(assignmentId);
        if (!"STUDENT".equals(r.targetType())) o.setTargetUserId(null);
        if (!"SECTION".equals(r.targetType())) o.setTargetSectionId(null);
        if (!"GROUP".equals(r.targetType())) o.setTargetGroupId(null);
        if (!"TAG".equals(r.targetType())) o.setTargetTagId(null);
        o.setCreatedBy(Access.ctx().userId());
        if (o.getDateDue() != null && o.getDateClose() != null && o.getDateClose().isBefore(o.getDateDue())) {
            throw ApiException.badRequest("INVALID_DATES", "dateClose must be on or after dateDue");
        }
        return assignmentMapper.toDto(refresher.refresh(overrides.saveAndFlush(o)));
    }

    @Transactional
    @Override
    public void removeOverride(UUID assignmentId, UUID overrideId) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        blueprintLocks.requireEditable(a.getCourseId(), BlueprintContentLockObjectType.ASSIGNMENT, assignmentId, java.util.EnumSet.of(DUE_DATES));
        AssignmentOverride o = overrides.findById(overrideId).filter(x -> assignmentId.equals(x.getAssignmentId()))
                .orElseThrow(() -> ApiException.notFound("Override"));
        overrides.delete(o);
        overrides.flush();
    }

    // ---------------------------------------------------------------- schedule rules

    /**
     * Base dates become moments in the course time zone (available = start of day, due/close = end of day). Overrides that
     * apply to the student (theirs, their section's, their group's) replace a date when they set it; among several, the
     * most generous wins: earliest available, latest due and close, most attempts.
     */
    @Override
    public Schedule schedule(Assignment a, UUID userId) {
        ZoneId zone = zoneOf(course(a.getCourseId()));
        OffsetDateTime available = startOfDay(a.getDateAvailable(), zone);
        OffsetDateTime due = endOfDay(a.getDateDue(), zone);
        OffsetDateTime close = endOfDay(a.getDateClose(), zone);
        LocalDate paced = paces.pacedDue(a.getCourseId(), a.getAssignmentId(), userId);
        if (paced != null) {                                    // course pacing sets the student's own due date
            due = endOfDay(paced, zone);
            if (close != null && close.isBefore(due)) close = due;
        }
        Integer max = a.getMaxSubmissions();
        List<AssignmentOverride> mine = paced != null ? List.of() : applicable(a, userId);
        if (!mine.isEmpty()) {
            available = pick(mine.stream().map(AssignmentOverride::getDateAvailable).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null), available);
            due = pick(mine.stream().map(AssignmentOverride::getDateDue).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null), due);
            close = pick(mine.stream().map(AssignmentOverride::getDateClose).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null), close);
            Integer extra = mine.stream().map(AssignmentOverride::getMaxAttemptsOverride).filter(Objects::nonNull).max(Integer::compare).orElse(null);
            if (extra != null) max = max == null ? null : Math.max(max, extra);
        }
        return new Schedule(available, due, close, max);
    }

    private List<AssignmentOverride> applicable(Assignment a, UUID userId) {
        List<AssignmentOverride> all = overrides.findByAssignmentIdOrderByDateCreatedAsc(a.getAssignmentId());
        if (all.isEmpty()) return List.of();
        Set<UUID> mySections = enrollments.activeInCourse(a.getCourseId(), userId).stream().map(Enrollment::getSectionId).collect(Collectors.toSet());
        return all.stream().filter(o -> switch (o.getTargetType()) {
            case "STUDENT" -> userId.equals(o.getTargetUserId());
            case "SECTION" -> mySections.contains(o.getTargetSectionId());
            case "GROUP" -> members.findByGroupIdAndUserId(o.getTargetGroupId(), userId).filter(m -> Boolean.TRUE.equals(m.getIsActive())).isPresent();
            case "TAG" -> tagMembers.existsByTagIdAndUserId(o.getTargetTagId(), userId);
            default -> false;
        }).toList();
    }

    @Override
    public String submitLockReason(Assignment a, UUID userId) {
        if (!Boolean.TRUE.equals(a.getIsPublished()) || a.getAssignmentStatus() == AssignmentStatus.CLOSED
                || a.getAssignmentStatus() == AssignmentStatus.ARCHIVED) {
            return "The assignment is not open";
        }
        if (!ONLINE_TYPES.contains(a.getAssignmentType())) return "This assignment is not handed in online here";
        String moduleLock = progress.moduleLockReason(a.getCourseId(), a.getModuleId(), a.getAssignmentId(), userId);
        if (moduleLock != null) return moduleLock;
        String released = availability.lockReason(a.getCourseId(), learning_management_system.backend.enums.TargetType.ASSIGNMENT,
                a.getAssignmentId(), a.getModuleId(), userId);
        if (released != null) return released;
        Schedule s = schedule(a, userId);
        OffsetDateTime now = OffsetDateTime.now();
        if (s.available() != null && now.isBefore(s.available())) return "Available from " + s.available();
        if (s.close() != null && now.isAfter(s.close())) return "Closed on " + s.close();
        if (s.due() != null && now.isAfter(s.due()) && Boolean.FALSE.equals(a.getLateSubmissionAllowed())) return "The due date has passed";
        if (s.maxAttempts() != null && attemptsUsed(a.getAssignmentId(), userId) >= s.maxAttempts()) return "No attempts left";
        return null;
    }

    @Override
    public int attemptsUsed(UUID assignmentId, UUID userId) {
        return (int) submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, userId).stream()
                .filter(s -> StudentModuleProgressServiceImplementation.HANDED_IN.contains(s.getSubmissionStatus()) && s.getSubmittedAt() != null)
                .count();
    }

    // ---------------------------------------------------------------- helpers

    static ZoneId zoneOf(Course c) {
        try {
            return c.getTimeZone() == null || c.getTimeZone().isBlank() ? ZoneOffset.UTC : ZoneId.of(c.getTimeZone());
        } catch (Exception e) {
            return ZoneOffset.UTC;
        }
    }

    private static OffsetDateTime startOfDay(LocalDate d, ZoneId zone) {
        return d == null ? null : d.atStartOfDay(zone).toOffsetDateTime();
    }

    private static OffsetDateTime endOfDay(LocalDate d, ZoneId zone) {
        return d == null ? null : d.atTime(LocalTime.of(23, 59, 59)).atZone(zone).toOffsetDateTime();
    }

    private static OffsetDateTime pick(OffsetDateTime override, OffsetDateTime base) {
        return override != null ? override : base;
    }

    private static SubmissionType submissionTypeFor(AssignmentType t) {
        return switch (t) {
            case ON_PAPER -> SubmissionType.ON_PAPER;
            case NO_SUBMISSION -> SubmissionType.OFFLINE;
            default -> SubmissionType.ONLINE;
        };
    }

    private static void validateDates(Assignment a) {
        if (a.getDateAvailable() != null && a.getDateDue() != null && a.getDateDue().isBefore(a.getDateAvailable())) {
            throw ApiException.badRequest("INVALID_DATES", "dateDue must be on or after dateAvailable");
        }
        if (a.getDateDue() != null && a.getDateClose() != null && a.getDateClose().isBefore(a.getDateDue())) {
            throw ApiException.badRequest("INVALID_DATES", "dateClose must be on or after dateDue");
        }
    }

    private boolean hasStudentWork(UUID assignmentId) {
        return submissions.findByAssignmentIdOrderByUserIdAscSubmissionNumberAsc(assignmentId).stream()
                .anyMatch(s -> s.getSubmittedAt() != null);
    }

    /** Content and assignments share one order inside a module. */
    private int nextOrder(UUID moduleId) {
        return Math.max(contents.maxDisplayOrderInModule(moduleId), assignments.maxDisplayOrderInModule(moduleId)) + 1;
    }

    private boolean isStaff(UUID courseId) {
        return access.canSeeEverything() || access.isCourseStaff(courseId);
    }

    private void module(UUID moduleId, UUID courseId) {
        modules.findById(moduleId).filter(m -> m.getCourseId().equals(courseId))
                .orElseThrow(() -> ApiException.badRequest("MODULE_NOT_IN_COURSE", "The module does not belong to this course"));
    }

    private Assignment load(UUID id) {
        return assignments.findById(id).orElseThrow(() -> ApiException.notFound("Assignment"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
