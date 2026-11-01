package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import learning_management_system.backend.enums.EnrollmentStatus;

/** Change an enrollment's status (WITHDRAWN, DROPPED, COMPLETED, ACTIVE ...). */
public record UpdateEnrollmentStatusRequest(@NotNull EnrollmentStatus status) {}
