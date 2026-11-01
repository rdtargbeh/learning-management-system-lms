package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import learning_management_system.backend.enums.EnrollmentType;

/**
 * Enroll a user in a section. enrollmentType defaults to STUDENT; OBSERVER needs observedUserId (a student
 * actively enrolled in the same section).
 */
public record CreateEnrollmentRequest(@NotNull UUID userId, EnrollmentType enrollmentType, UUID observedUserId,
                                      Boolean limitToSection, String sisSourceId) {}
