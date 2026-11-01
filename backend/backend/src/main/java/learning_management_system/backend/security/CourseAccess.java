package learning_management_system.backend.security;

import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.enums.CourseInstructorRole;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Course-level authorization, the way Canvas and Blackboard do it: institution permissions (COURSE_MANAGE,
 * ENROLLMENT_MANAGE, COURSE_READ) work everywhere, and course staff get rights only in the sections they are assigned to.
 * <ul>
 *   <li>Teachers (PRIMARY, CO_INSTRUCTOR) can edit and publish their course and manage its roster and groups.</li>
 *   <li>All staff (also TEACHING_ASSISTANT, GRADER, REVIEWER) can see the course, roster and groups.</li>
 *   <li>Students and observers can see a course only while it is published (or concluded) and they are actively enrolled.</li>
 * </ul>
 */
@Component
public class CourseAccess {
    public static final Set<CourseInstructorRole> TEACHER_ROLES = EnumSet.of(CourseInstructorRole.PRIMARY, CourseInstructorRole.CO_INSTRUCTOR);
    public static final Set<CourseInstructorRole> STAFF_ROLES = EnumSet.allOf(CourseInstructorRole.class);

    private final CourseInstructorRepository staff;
    private final EnrollmentRepository enrollments;

    public CourseAccess(CourseInstructorRepository staff, EnrollmentRepository enrollments) {
        this.staff = staff;
        this.enrollments = enrollments;
    }

    private static UUID me() { return Access.ctx().userId(); }

    public boolean isCourseTeacher(UUID courseId) { return staff.isStaffInCourse(courseId, me(), TEACHER_ROLES); }

    public boolean isCourseStaff(UUID courseId) { return staff.isStaffInCourse(courseId, me(), STAFF_ROLES); }

    public boolean isSectionTeacher(UUID sectionId) { return staff.isStaffInSection(sectionId, me(), TEACHER_ROLES); }

    public boolean isSectionStaff(UUID sectionId) { return staff.isStaffInSection(sectionId, me(), STAFF_ROLES); }

    /** COURSE_MANAGE, or a teacher of the course. */
    public void requireCourseEditor(UUID courseId) {
        Access.requireTenant();
        if (Access.hasTenantPermission(Permissions.COURSE_MANAGE) || isCourseTeacher(courseId)) return;
        throw ApiException.forbidden("Only the course's teachers or a course administrator can do this");
    }

    /** Anyone who may see the course at all. */
    public void requireCourseViewer(Course c) {
        Access.requireTenant();
        if (canSeeEverything() || isCourseStaff(c.getCourseId())) return;
        boolean visible = c.getCourseStatus() == CourseStatus.PUBLISHED || c.getCourseStatus() == CourseStatus.CONCLUDED;
        if (visible && enrollments.isActiveInCourse(c.getCourseId(), me())) return;
        throw ApiException.notFound("Course");     // do not reveal courses the caller cannot see
    }

    /** ENROLLMENT_MANAGE, or a teacher of this section. */
    public void requireRosterManager(CourseSection s) {
        Access.requireTenant();
        if (Access.hasTenantPermission(Permissions.ENROLLMENT_MANAGE) || isSectionTeacher(s.getSectionId())) return;
        throw ApiException.forbidden("Only the section's teachers or an enrollment administrator can change the roster");
    }

    /** Institution course/enrollment readers and any staff member of the section. */
    public void requireRosterViewer(CourseSection s) {
        Access.requireTenant();
        if (canSeeEverything() || isSectionStaff(s.getSectionId())) return;
        throw ApiException.forbidden("Only course staff can see the roster");
    }

    /** COURSE_MANAGE only (structure changes such as sections, staff, cross-listing, archiving). */
    public void requireCourseAdmin() {
        Access.requireTenantPermission(Permissions.COURSE_MANAGE);
    }

    public boolean canSeeEverything() {
        return Access.hasTenantPermission(Permissions.COURSE_READ, Permissions.COURSE_MANAGE, Permissions.ENROLLMENT_MANAGE);
    }
}
