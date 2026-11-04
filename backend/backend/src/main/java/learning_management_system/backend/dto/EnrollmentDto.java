package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.EnrollmentSource;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.EnrollmentType;

/** An enrollment of a user in a section (table enrollment) with the user's name. */
public record EnrollmentDto(UUID enrollmentId, UUID sectionId, UUID courseId, UUID userId, String username, String displayName,
                            EnrollmentType enrollmentType, UUID observedUserId, EnrollmentStatus enrollmentStatus,
                            EnrollmentSource enrollmentSource, Boolean limitToSection, LocalDate dateEnrolled,
                            LocalDate dateWithdrawn, LocalDate dateCompleted, OffsetDateTime dateLastActivity) {}
