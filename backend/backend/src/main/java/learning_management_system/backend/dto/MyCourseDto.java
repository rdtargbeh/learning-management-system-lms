package learning_management_system.backend.dto;

import java.util.UUID;
import learning_management_system.backend.enums.EnrollmentStatus;

/**
 * A course on the caller's dashboard, with the section and the caller's role in it (a staff role such as PRIMARY
 * or TEACHING_ASSISTANT, or an enrollment type such as STUDENT).
 */
public record MyCourseDto(CourseDto course, UUID sectionId, String sectionNumber, String role, EnrollmentStatus enrollmentStatus) {}
