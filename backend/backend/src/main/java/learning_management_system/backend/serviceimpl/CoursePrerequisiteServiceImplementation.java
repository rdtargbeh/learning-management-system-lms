package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CoursePrerequisiteDto;
import learning_management_system.backend.dto.CreateCoursePrerequisiteRequest;
import learning_management_system.backend.dto.PrerequisiteCheckDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CoursePrerequisite;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.Gradebook;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;
import learning_management_system.backend.enums.PrerequisiteType;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CoursePrerequisiteRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.GradebookRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.CoursePrerequisiteService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoursePrerequisiteServiceImplementation implements CoursePrerequisiteService {
    /** Letter grades from best to worst. */
    private static final List<String> SCALE = List.of("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F");
    private static final Set<EnrollmentType> LEARNER_TYPES = EnumSet.of(EnrollmentType.STUDENT, EnrollmentType.VISITING);

    private final CoursePrerequisiteRepository prerequisites;
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final GradebookRepository gradebooks;
    private final CourseAccess access;
    private final EntityRefresher refresher;
    private final AuditLogService audit;

    public CoursePrerequisiteServiceImplementation(CoursePrerequisiteRepository prerequisites, CourseRepository courses,
                                                   EnrollmentRepository enrollments, GradebookRepository gradebooks,
                                                   CourseAccess access, EntityRefresher refresher, AuditLogService audit) {
        this.prerequisites = prerequisites;
        this.courses = courses;
        this.enrollments = enrollments;
        this.gradebooks = gradebooks;
        this.access = access;
        this.refresher = refresher;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CoursePrerequisiteDto> list(UUID courseId) {
        Access.requireTenant();
        course(courseId);
        List<CoursePrerequisite> list = prerequisites.findByCourseIdOrderByDateCreatedAsc(courseId);
        Map<UUID, Course> byId = courses.findAllById(list.stream().map(CoursePrerequisite::getPrerequisiteCourseId).toList()).stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));
        return list.stream().map(p -> toDto(p, byId.get(p.getPrerequisiteCourseId()))).toList();
    }

    @Transactional
    @Override
    public CoursePrerequisiteDto add(UUID courseId, CreateCoursePrerequisiteRequest r) {
        access.requireCourseAdmin();
        course(courseId);
        Course pre = courses.findById(r.prerequisiteCourseId()).orElseThrow(() -> ApiException.badRequest("UNKNOWN_COURSE", "Unknown prerequisite course"));
        if (pre.getCourseId().equals(courseId)) throw ApiException.badRequest("SELF_PREREQUISITE", "A course cannot be its own prerequisite");
        if (prerequisites.findByCourseIdAndPrerequisiteCourseId(courseId, pre.getCourseId()).isPresent()) {
            throw ApiException.conflict("PREREQUISITE_EXISTS", "This prerequisite is already set");
        }
        PrerequisiteType type = r.prerequisiteType() == null ? PrerequisiteType.REQUIRED : r.prerequisiteType();
        if (r.minGrade() != null && type != PrerequisiteType.REQUIRED) {
            throw ApiException.badRequest("MIN_GRADE_REQUIRED_ONLY", "A minimum grade applies to REQUIRED prerequisites only");
        }
        if (type == PrerequisiteType.REQUIRED && requires(pre.getCourseId(), courseId, new HashSet<>())) {
            throw ApiException.badRequest("PREREQUISITE_CYCLE", pre.getCourseCode() + " already requires this course (directly or through others)");
        }
        CoursePrerequisite p = new CoursePrerequisite();
        p.setCourseId(courseId);
        p.setPrerequisiteCourseId(pre.getCourseId());
        p.setPrerequisiteType(type);
        p.setMinGrade(r.minGrade());
        refresher.refresh(prerequisites.saveAndFlush(p));
        audit.record("PREREQUISITE_ADDED", "COURSE", courseId, null, Severity.INFO, null,
                Map.of("prerequisite", pre.getCourseCode(), "type", type.name()));
        return toDto(p, pre);
    }

    @Transactional
    @Override
    public void remove(UUID courseId, UUID prerequisiteId) {
        access.requireCourseAdmin();
        CoursePrerequisite p = prerequisites.findByPrerequisiteIdAndCourseId(prerequisiteId, courseId)
                .orElseThrow(() -> ApiException.notFound("Prerequisite"));
        prerequisites.delete(p);
        audit.record("PREREQUISITE_REMOVED", "COURSE", courseId, null, Severity.INFO, null, null);
    }

    @Transactional(readOnly = true)
    @Override
    public PrerequisiteCheckDto check(UUID courseId, UUID userId) {
        Access.requireTenant();
        UUID who = userId == null ? Access.ctx().userId() : userId;
        if (!Access.isSelf(who) && !Access.hasTenantPermission(Permissions.ENROLLMENT_MANAGE, Permissions.USER_READ)
                && !access.isCourseTeacher(courseId)) {
            throw ApiException.forbidden("You can only check your own prerequisites");
        }
        course(courseId);
        return evaluate(courseId, who);
    }

    @Override
    public void requireMet(UUID courseId, UUID userId) {
        PrerequisiteCheckDto c = evaluate(courseId, userId);
        if (c.met()) return;
        String missing = c.items().stream().filter(i -> !i.met() && !"RECOMMENDED".equals(i.prerequisiteType()))
                .map(i -> i.courseCode() + (i.minGrade() != null ? " (" + i.minGrade() + " or better)" : "")
                        + ("COREQUISITE".equals(i.prerequisiteType()) ? " as a corequisite" : ""))
                .collect(Collectors.joining(", "));
        throw ApiException.conflict("PREREQUISITES_NOT_MET", "Prerequisites not met: " + missing);
    }

    // ---------------------------------------------------------------- helpers

    private PrerequisiteCheckDto evaluate(UUID courseId, UUID userId) {
        List<PrerequisiteCheckDto.Item> items = new ArrayList<>();
        boolean met = true;
        for (CoursePrerequisite p : prerequisites.findByCourseIdOrderByDateCreatedAsc(courseId)) {
            Course pre = courses.findById(p.getPrerequisiteCourseId()).orElse(null);
            if (pre == null) continue;
            List<Enrollment> done = learner(enrollments.inCourseWithStatuses(pre.getCourseId(), userId, EnumSet.of(EnrollmentStatus.COMPLETED)));
            boolean taking = !learner(enrollments.inCourseWithStatuses(pre.getCourseId(), userId, EnumSet.of(EnrollmentStatus.ACTIVE))).isEmpty();
            String status;
            String grade = null;
            boolean ok;
            if (!done.isEmpty() && p.getMinGrade() != null) {
                grade = bestGrade(done);
                if (grade == null) { status = "GRADE_UNKNOWN"; ok = false; }
                else if (rank(grade) <= rank(p.getMinGrade())) { status = "COMPLETED"; ok = true; }
                else { status = "GRADE_TOO_LOW"; ok = false; }
            } else if (!done.isEmpty()) {
                status = "COMPLETED"; ok = true;
            } else if (taking) {
                status = "ENROLLED"; ok = p.getPrerequisiteType() == PrerequisiteType.COREQUISITE;
            } else {
                status = "MISSING"; ok = false;
            }
            if (p.getPrerequisiteType() == PrerequisiteType.RECOMMENDED) {
                items.add(new PrerequisiteCheckDto.Item(pre.getCourseId(), pre.getCourseCode(), pre.getCourseTitle(), "RECOMMENDED", null, status, grade, true));
                continue;
            }
            met &= ok;
            items.add(new PrerequisiteCheckDto.Item(pre.getCourseId(), pre.getCourseCode(), pre.getCourseTitle(),
                    p.getPrerequisiteType().name(), p.getMinGrade(), status, grade, ok));
        }
        return new PrerequisiteCheckDto(courseId, userId, met, items);
    }

    private static List<Enrollment> learner(List<Enrollment> list) {
        return list.stream().filter(e -> LEARNER_TYPES.contains(e.getEnrollmentType())).toList();
    }

    /** Best final letter (override first) over the user's completed sections; null when no grade was recorded. */
    private String bestGrade(List<Enrollment> completed) {
        String best = null;
        for (Enrollment e : completed) {
            Gradebook g = gradebooks.findBySectionIdAndUserId(e.getSectionId(), e.getUserId()).orElse(null);
            if (g == null) continue;
            String letter = g.getOverrideLetter() != null ? g.getOverrideLetter() : g.getFinalGradeLetter();
            if (letter == null || rank(letter.trim().toUpperCase()) == Integer.MAX_VALUE) continue;
            letter = letter.trim().toUpperCase();
            if (best == null || rank(letter) < rank(best)) best = letter;
        }
        return best;
    }

    private static int rank(String letter) {
        int i = SCALE.indexOf(letter);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    /** Does {@code from} require {@code target}, directly or through other REQUIRED prerequisites? */
    private boolean requires(UUID from, UUID target, Set<UUID> seen) {
        if (!seen.add(from)) return false;
        for (CoursePrerequisite p : prerequisites.findByCourseIdAndPrerequisiteType(from, PrerequisiteType.REQUIRED)) {
            if (p.getPrerequisiteCourseId().equals(target) || requires(p.getPrerequisiteCourseId(), target, seen)) return true;
        }
        return false;
    }

    private CoursePrerequisiteDto toDto(CoursePrerequisite p, Course pre) {
        return new CoursePrerequisiteDto(p.getPrerequisiteId(), p.getCourseId(), p.getPrerequisiteCourseId(),
                pre == null ? null : pre.getCourseCode(), pre == null ? null : pre.getCourseTitle(), p.getPrerequisiteType(),
                p.getMinGrade(), p.getDateCreated());
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
