package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.PlanType;

/** An institution's plan (table tenant_subscription, one per institution). */
public record TenantSubscriptionDto(UUID subscriptionId, UUID tenantId, PlanType planType, String planName, Integer maxStudents,
                                    Integer maxInstructors, Integer storageLimitGb, Boolean isActive, LocalDate dateStart,
                                    LocalDate dateEnd, LocalDate dateRenewal) {}
