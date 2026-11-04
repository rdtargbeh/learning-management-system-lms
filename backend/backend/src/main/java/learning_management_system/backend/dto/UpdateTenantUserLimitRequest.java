package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;

/** Replace an institution's account limits (platform staff). null = no limit. */
public record UpdateTenantUserLimitRequest(@Positive Integer maxStudents, @Positive Integer maxInstructors, @Positive Integer maxStaff,
                                           @Positive Integer maxAdmins) {}
