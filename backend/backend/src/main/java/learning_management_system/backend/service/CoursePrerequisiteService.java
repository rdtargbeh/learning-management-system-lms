package learning_management_system.backend.service;

import learning_management_system.backend.dto.CoursePrerequisiteDto;
import learning_management_system.backend.dto.CreateCoursePrerequisiteRequest;
import learning_management_system.backend.dto.PrerequisiteCheckDto;

import java.util.List;
import java.util.UUID;

/**
 * course_prerequisite: catalog rules between courses. REQUIRED and COREQUISITE are enforced when students enroll
 * themselves, join a waitlist, or are enrolled by teachers; institution admins (ENROLLMENT_MANAGE) may override.
 */
public interface CoursePrerequisiteService {

    /** Any member of the institution can read a course's prerequisites (catalog information). */
    List<CoursePrerequisiteDto> list(UUID courseId);

    /** COURSE_MANAGE. A REQUIRED chain may not loop back to the course. */
    CoursePrerequisiteDto add(UUID courseId, CreateCoursePrerequisiteRequest r);

    void remove(UUID courseId, UUID prerequisiteId);

    /** The user's standing (own; others' for roster managers and ENROLLMENT_MANAGE). userId null = the caller. */
    PrerequisiteCheckDto check(UUID courseId, UUID userId);

    /** Throws 409 PREREQUISITES_NOT_MET when an enforced prerequisite is not met (no permission check). */
    void requireMet(UUID courseId, UUID userId);
}
