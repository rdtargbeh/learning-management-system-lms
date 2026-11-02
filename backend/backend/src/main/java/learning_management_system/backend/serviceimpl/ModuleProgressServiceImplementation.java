package learning_management_system.backend.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.CourseModule;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Feedback;
import learning_management_system.backend.entity.ModuleCompletionRequirement;
import learning_management_system.backend.entity.ModulePrerequisite;
import learning_management_system.backend.entity.StudentContentProgress;
import learning_management_system.backend.entity.StudentModuleProgress;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.ProgressStatus;
import learning_management_system.backend.enums.StudentContentProgressStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.AssignmentRepository;
import learning_management_system.backend.repository.AssignmentSubmissionRepository;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.CourseModuleRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.FeedbackRepository;
import learning_management_system.backend.repository.ModuleCompletionRequirementRepository;
import learning_management_system.backend.repository.ModulePrerequisiteRepository;
import learning_management_system.backend.repository.StudentContentProgressRepository;
import learning_management_system.backend.repository.StudentModuleProgressRepository;
import learning_management_system.backend.service.ModuleProgressService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ModuleProgressServiceImplementation implements ModuleProgressService {
    /** Submission states that count as "handed in". */
    static final Set<AssignmentSubmissionStatus> HANDED_IN =
            EnumSet.of(AssignmentSubmissionStatus.SUBMITTED, AssignmentSubmissionStatus.GRADED, AssignmentSubmissionStatus.RETURNED);

    private final CourseModuleRepository modules;
    private final ModulePrerequisiteRepository prerequisites;
    private final ModuleCompletionRequirementRepository requirements;
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final FeedbackRepository feedback;
    private final StudentContentProgressRepository contentProgress;
    private final StudentModuleProgressRepository moduleProgress;
    private final EnrollmentRepository enrollments;
    private final UserService userService;
    private final ObjectMapper json;

    public ModuleProgressServiceImplementation(CourseModuleRepository modules, ModulePrerequisiteRepository prerequisites,
                                               ModuleCompletionRequirementRepository requirements,
                                               ContentRepository contents, AssignmentRepository assignments,
                                               AssignmentSubmissionRepository submissions, FeedbackRepository feedback,
                                               StudentContentProgressRepository contentProgress,
                                               StudentModuleProgressRepository moduleProgress,
                                               EnrollmentRepository enrollments, UserService userService, ObjectMapper json) {
        this.modules = modules;
        this.prerequisites = prerequisites;
        this.requirements = requirements;
        this.contents = contents;
        this.assignments = assignments;
        this.submissions = submissions;
        this.feedback = feedback;
        this.contentProgress = contentProgress;
        this.moduleProgress = moduleProgress;
        this.enrollments = enrollments;
        this.userService = userService;
        this.json = json;
    }

    // ---------------------------------------------------------------- course data

    /** A published item of a module as students see it: a content item or an assignment. */
    private record Item(UUID id, String title, int order, boolean assignment) {}

    /** Everything about one course needed to evaluate students, loaded once. */
    private record CourseData(List<CourseModule> modules, Map<UUID, List<UUID>> prereqs,
                              Map<UUID, List<ModuleCompletionRequirement>> reqs, Map<UUID, List<Item>> items,
                              Set<UUID> contentIds, Set<UUID> assignmentIds) {}

    /** What one student has done, as far as requirements are concerned. */
    private record Done(Map<UUID, StudentContentProgress> content, Set<UUID> handedIn, Set<UUID> excused,
                        Map<UUID, BigDecimal> postedScore) {}

    private CourseData load(UUID courseId) {
        List<CourseModule> ms = modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(courseId).stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPublished())).toList();
        List<UUID> ids = ms.stream().map(CourseModule::getModuleId).toList();
        Map<UUID, List<UUID>> pre = new HashMap<>();
        Map<UUID, List<ModuleCompletionRequirement>> req = new HashMap<>();
        Map<UUID, List<Item>> items = new HashMap<>();
        Set<UUID> contentIds = new HashSet<>(), assignmentIds = new HashSet<>();
        if (!ids.isEmpty()) {
            for (ModulePrerequisite p : prerequisites.findByModuleIdIn(ids)) pre.computeIfAbsent(p.getModuleId(), k -> new ArrayList<>()).add(p.getPrerequisiteModuleId());
            for (ModuleCompletionRequirement r : requirements.findByModuleIdInOrderByDisplayOrderAsc(ids)) req.computeIfAbsent(r.getModuleId(), k -> new ArrayList<>()).add(r);
            for (Content c : contents.findByModuleIdIn(ids)) {
                if (!Boolean.TRUE.equals(c.getIsPublished()) || !studentVisible(c)) continue;
                items.computeIfAbsent(c.getModuleId(), k -> new ArrayList<>()).add(new Item(c.getContentId(), c.getContentTitle(), nz(c.getDisplayOrder()), false));
                contentIds.add(c.getContentId());
            }
            for (Assignment a : assignments.findByModuleIdIn(ids)) {
                if (!Boolean.TRUE.equals(a.getIsPublished())) continue;
                items.computeIfAbsent(a.getModuleId(), k -> new ArrayList<>()).add(new Item(a.getAssignmentId(), a.getAssignmentTitle(), nz(a.getDisplayOrder()), true));
                assignmentIds.add(a.getAssignmentId());
            }
            items.values().forEach(l -> l.sort(Comparator.comparingInt(Item::order).thenComparing(Item::title)));
        }
        return new CourseData(ms, pre, req, items, contentIds, assignmentIds);
    }

    private Done done(CourseData d, UUID userId) {
        Map<UUID, StudentContentProgress> prog = d.contentIds().isEmpty() ? Map.of()
                : contentProgress.findByUserIdAndContentIdIn(userId, d.contentIds()).stream()
                        .collect(Collectors.toMap(StudentContentProgress::getContentId, p -> p));
        Set<UUID> handedIn = new HashSet<>(), excused = new HashSet<>();
        Map<UUID, BigDecimal> posted = new HashMap<>();
        if (!d.assignmentIds().isEmpty()) {
            List<AssignmentSubmission> subs = submissions.findByUserIdAndAssignmentIdIn(userId, d.assignmentIds());
            Map<UUID, Feedback> latestFeedback = new HashMap<>();
            List<UUID> returned = subs.stream().filter(s -> s.getSubmissionStatus() == AssignmentSubmissionStatus.RETURNED)
                    .map(AssignmentSubmission::getSubmissionId).toList();
            if (!returned.isEmpty()) {
                for (Feedback f : feedback.findBySubmissionIdInOrderByDateGradedDesc(returned)) latestFeedback.putIfAbsent(f.getSubmissionId(), f);
            }
            for (AssignmentSubmission s : subs) {
                if (Boolean.TRUE.equals(s.getIsExcused())) excused.add(s.getAssignmentId());
                if (HANDED_IN.contains(s.getSubmissionStatus()) && s.getSubmittedAt() != null) handedIn.add(s.getAssignmentId());
                Feedback f = latestFeedback.get(s.getSubmissionId());
                if (f != null && f.getGradeEarned() != null) posted.merge(s.getAssignmentId(), f.getGradeEarned(), BigDecimal::max);
            }
        }
        return new Done(prog, handedIn, excused, posted);
    }

    // ---------------------------------------------------------------- evaluation

    @Override
    public Map<UUID, ModuleState> evaluate(UUID courseId, UUID userId) {
        CourseData d = load(courseId);
        Map<UUID, ModuleState> states = compute(d, done(d, userId), sectionsOf(courseId, userId));
        if (isTrackedStudent(courseId, userId)) persist(states, userId);
        return states;
    }

    @Override
    public String moduleLockReason(UUID courseId, UUID moduleId, UUID itemId, UUID userId) {
        if (moduleId == null) return null;
        ModuleState s = evaluate(courseId, userId).get(moduleId);
        if (s == null) return "Not available";
        return s.lockedItems().get(itemId);
    }

    private Set<UUID> sectionsOf(UUID courseId, UUID userId) {
        return enrollments.activeInCourse(courseId, userId).stream().map(Enrollment::getSectionId).collect(Collectors.toSet());
    }

    private Map<UUID, ModuleState> compute(CourseData d, Done done, Set<UUID> sections) {
        List<CourseModule> visible = d.modules().stream()
                .filter(m -> m.getSectionId() == null || sections.contains(m.getSectionId())).toList();
        Map<UUID, CourseModule> byId = visible.stream().collect(Collectors.toMap(CourseModule::getModuleId, m -> m));
        Map<UUID, ModuleState> out = new LinkedHashMap<>();
        for (CourseModule m : visible) state(m, d, byId, done, out, new HashSet<>());
        return out;
    }

    private ModuleState state(CourseModule m, CourseData d, Map<UUID, CourseModule> byId, Done done,
                              Map<UUID, ModuleState> memo, Set<UUID> visiting) {
        ModuleState known = memo.get(m.getModuleId());
        if (known != null) return known;
        visiting.add(m.getModuleId());
        List<Item> items = d.items().getOrDefault(m.getModuleId(), List.of());
        Set<UUID> itemIds = items.stream().map(Item::id).collect(Collectors.toSet());

        String lockReason = null;
        if (Boolean.TRUE.equals(m.getIsLocked())) lockReason = "Locked by the teacher";
        else if (m.getDateUnlock() != null && m.getDateUnlock().isAfter(OffsetDateTime.now())) lockReason = "Unlocks on " + m.getDateUnlock();
        else {
            for (UUID p : d.prereqs().getOrDefault(m.getModuleId(), List.of())) {
                CourseModule pm = byId.get(p);
                if (pm == null || visiting.contains(p)) continue;           // unpublished/hidden prerequisites are ignored
                if (state(pm, d, byId, done, memo, visiting).status() != ProgressStatus.COMPLETED) {
                    lockReason = "Complete \"" + pm.getModuleTitle() + "\" first";
                    break;
                }
            }
        }
        // requirements only count for published items of this module
        List<ModuleCompletionRequirement> reqs = d.reqs().getOrDefault(m.getModuleId(), List.of()).stream()
                .filter(r -> itemIds.contains(itemOf(r))).toList();
        Set<UUID> met = new LinkedHashSet<>();
        Set<UUID> completedItems = new LinkedHashSet<>();
        Map<UUID, ModuleCompletionRequirement> reqByItem = new HashMap<>();
        for (ModuleCompletionRequirement r : reqs) {
            reqByItem.put(itemOf(r), r);
            if (satisfied(r, done)) { met.add(r.getRequirementId()); completedItems.add(itemOf(r)); }
        }
        Map<UUID, String> lockedItems = new LinkedHashMap<>();
        ProgressStatus status;
        if (lockReason != null) {
            status = ProgressStatus.LOCKED;
            for (UUID id : itemIds) lockedItems.put(id, "This module is locked: " + lockReason);
        } else {
            boolean anyActivity = items.stream().anyMatch(i -> done.content().containsKey(i.id()) || done.handedIn().contains(i.id()));
            status = met.size() == reqs.size() ? ProgressStatus.COMPLETED
                    : (!met.isEmpty() || anyActivity) ? ProgressStatus.STARTED : ProgressStatus.UNLOCKED;
            if (Boolean.TRUE.equals(m.getRequireSequentialProgress())) {
                String blocker = null;
                for (Item i : items) {
                    if (blocker != null) { lockedItems.put(i.id(), "Complete \"" + blocker + "\" first"); continue; }
                    ModuleCompletionRequirement r = reqByItem.get(i.id());
                    if (r != null && !met.contains(r.getRequirementId())) blocker = i.title();
                }
            }
        }
        ModuleState s = new ModuleState(m.getModuleId(), status, lockReason, met, completedItems, lockedItems);
        memo.put(m.getModuleId(), s);
        visiting.remove(m.getModuleId());
        return s;
    }

    private static UUID itemOf(ModuleCompletionRequirement r) {
        return r.getContentId() != null ? r.getContentId() : r.getAssignmentId();
    }

    private static boolean satisfied(ModuleCompletionRequirement r, Done done) {
        if (r.getContentId() != null) {
            StudentContentProgress p = done.content().get(r.getContentId());
            if (p == null) return false;
            return switch (r.getRequirementType()) {
                case VIEW -> p.getViewCount() != null && p.getViewCount() > 0;
                case MARK_DONE -> p.getStatus() == StudentContentProgressStatus.COMPLETED;
                default -> false;
            };
        }
        if (r.getAssignmentId() != null) {
            UUID a = r.getAssignmentId();
            if (done.excused().contains(a)) return true;
            return switch (r.getRequirementType()) {
                case SUBMIT -> done.handedIn().contains(a);
                case MIN_SCORE -> done.postedScore().containsKey(a) && r.getMinScore() != null
                        && done.postedScore().get(a).compareTo(r.getMinScore()) >= 0;
                default -> false;
            };
        }
        return false;                             // quiz and discussion requirements arrive with those features
    }

    /** Students never see INSTRUCTOR_ONLY items; RESTRICTED items need per-user permissions (not built yet). */
    static boolean studentVisible(Content c) {
        return c.getAccessLevel() != ContentAccessLevel.INSTRUCTOR_ONLY && c.getAccessLevel() != ContentAccessLevel.RESTRICTED;
    }

    private static int nz(Integer i) { return i == null ? 0 : i; }

    private void persist(Map<UUID, ModuleState> states, UUID userId) {
        if (states.isEmpty()) return;
        Map<UUID, StudentModuleProgress> existing = moduleProgress.findByUserIdAndModuleIdIn(userId, states.keySet()).stream()
                .collect(Collectors.toMap(StudentModuleProgress::getModuleId, p -> p));
        OffsetDateTime now = OffsetDateTime.now();
        for (ModuleState s : states.values()) {
            StudentModuleProgress p = existing.get(s.moduleId());
            ArrayNode met = json.createArrayNode();
            s.metRequirementIds().forEach(id -> met.add(id.toString()));
            if (p != null && p.getProgressStatus() == s.status() && Objects.equals(p.getRequirementsMet(), met)) continue;
            if (p == null) {
                p = new StudentModuleProgress();
                p.setModuleId(s.moduleId());
                p.setUserId(userId);
            }
            p.setProgressStatus(s.status());
            p.setRequirementsMet(met);
            if (s.status() != ProgressStatus.LOCKED && p.getDateUnlocked() == null) p.setDateUnlocked(now);
            if (s.status() == ProgressStatus.COMPLETED) { if (p.getDateCompleted() == null) p.setDateCompleted(now); }
            else p.setDateCompleted(null);
            moduleProgress.save(p);
        }
        moduleProgress.flush();
    }

    // ---------------------------------------------------------------- content progress

    @Override
    public boolean isTrackedStudent(UUID courseId, UUID userId) {
        return enrollments.activeInCourse(courseId, userId).stream()
                .anyMatch(e -> EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType()));
    }

    @Override
    public StudentContentProgress recordView(Content c, UUID userId) {
        StudentContentProgress p = progressRow(c, userId);
        OffsetDateTime now = OffsetDateTime.now();
        if (p.getDateFirstAccessed() == null) p.setDateFirstAccessed(now);
        p.setDateLastAccessed(now);
        p.setViewCount((p.getViewCount() == null ? 0 : p.getViewCount()) + 1);
        p.setLastInteractionType("VIEW");
        if (p.getStatus() == null || p.getStatus() == StudentContentProgressStatus.NOT_STARTED) p.setStatus(StudentContentProgressStatus.IN_PROGRESS);
        contentProgress.saveAndFlush(p);
        evaluate(c.getCourseId(), userId);
        return p;
    }

    @Override
    public StudentContentProgress markDone(Content c, UUID userId, boolean done) {
        StudentContentProgress p = progressRow(c, userId);
        OffsetDateTime now = OffsetDateTime.now();
        if (p.getDateFirstAccessed() == null) p.setDateFirstAccessed(now);
        p.setDateLastAccessed(now);
        p.setLastInteractionType(done ? "MARK_DONE" : "UNMARK_DONE");
        p.setStatus(done ? StudentContentProgressStatus.COMPLETED : StudentContentProgressStatus.IN_PROGRESS);
        p.setDateCompleted(done ? now : null);
        p.setPercentComplete(done ? BigDecimal.valueOf(100) : p.getPercentComplete());
        contentProgress.saveAndFlush(p);
        evaluate(c.getCourseId(), userId);
        return p;
    }

    private StudentContentProgress progressRow(Content c, UUID userId) {
        return contentProgress.findByUserIdAndContentId(userId, c.getContentId()).orElseGet(() -> {
            StudentContentProgress p = new StudentContentProgress();
            p.setUserId(userId);
            p.setContentId(c.getContentId());
            p.setStatus(StudentContentProgressStatus.NOT_STARTED);
            p.setViewCount(0);
            return p;
        });
    }

    // ---------------------------------------------------------------- teacher view

    @Override
    public List<ModuleStudentProgressDto> moduleProgress(UUID moduleId) {
        CourseModule m = modules.findById(moduleId).orElseThrow(() -> ApiException.notFound("Module"));
        CourseData d = load(m.getCourseId());
        List<Enrollment> students = enrollments.activeInCourseOfTypes(m.getCourseId(), EnrollmentProvisioningServiceImplementation.SEAT_TYPES);
        Map<UUID, Set<UUID>> sectionsByUser = new HashMap<>();
        students.forEach(e -> sectionsByUser.computeIfAbsent(e.getUserId(), k -> new HashSet<>()).add(e.getSectionId()));
        Map<UUID, UserSummaryDto> names = userService.summariesById(sectionsByUser.keySet());
        Map<UUID, OffsetDateTime> completedAt = moduleProgress.findByModuleId(moduleId).stream()
                .filter(p -> p.getDateCompleted() != null)
                .collect(Collectors.toMap(StudentModuleProgress::getUserId, StudentModuleProgress::getDateCompleted));
        Set<UUID> itemIds = d.items().getOrDefault(moduleId, List.of()).stream().map(Item::id).collect(Collectors.toSet());
        int total = (int) d.reqs().getOrDefault(moduleId, List.of()).stream().filter(r -> itemIds.contains(itemOf(r))).count();
        List<ModuleStudentProgressDto> out = new ArrayList<>();
        for (Map.Entry<UUID, Set<UUID>> e : sectionsByUser.entrySet()) {
            if (m.getSectionId() != null && !e.getValue().contains(m.getSectionId())) continue;
            ModuleState s = compute(d, done(d, e.getKey()), e.getValue()).get(moduleId);
            UserSummaryDto u = names.get(e.getKey());
            out.add(new ModuleStudentProgressDto(e.getKey(), u == null ? null : u.username(), u == null ? null : u.displayName(),
                    s == null ? null : s.status(), s == null ? 0 : s.metRequirementIds().size(), total,
                    s != null && s.status() == ProgressStatus.COMPLETED ? completedAt.get(e.getKey()) : null));
        }
        out.sort(Comparator.comparing(x -> x.username() == null ? "" : x.username()));
        return out;
    }
}
