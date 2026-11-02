package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import learning_management_system.backend.enums.PlanType;

/**
 * Set an institution's plan (platform staff). maxStudents / maxInstructors also limit accounts when no explicit
 * user limit is set.
 */
public record UpsertTenantSubscriptionRequest(@NotNull PlanType planType, @Size(max = 255) String planName, @Positive Integer maxStudents,
                                              @Positive Integer maxInstructors, @Positive Integer storageLimitGb, Boolean isActive,
                                              @NotNull LocalDate dateStart, LocalDate dateEnd, LocalDate dateRenewal) {}
