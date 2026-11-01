package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateEnrollmentRequest;
import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;

import java.util.List;
import java.util.UUID;

/**
 * Enrollments. Section teachers and ENROLLMENT_MANAGE can enroll and withdraw; students can join with a course's
 * self-enrollment code and drop enrollments they made themselves. Seat limits and the waitlist are applied
 * automatically.
 */
public interface EnrollmentService {

    EnrollmentDto enroll(UUID sectionId, CreateEnrollmentRequest r);

    /** Joins the first open section with a free seat of the course that has this code. */
    EnrollmentDto selfEnroll(String code);

    List<EnrollmentDto> roster(UUID sectionId, EnrollmentStatus status, EnrollmentType type);

    List<EnrollmentDto> forUser(UUID userId);

    EnrollmentDto get(UUID enrollmentId);

    EnrollmentDto changeStatus(UUID enrollmentId, EnrollmentStatus status);
}
