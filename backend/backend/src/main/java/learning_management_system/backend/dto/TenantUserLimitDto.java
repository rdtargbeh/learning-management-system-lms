package learning_management_system.backend.dto;

import java.time.OffsetDateTime;

/**
 * Account limits of an institution and how many accounts are in use (table tenant_user_limit). null = no limit.
 * Instructors include TAs; staff include advisors.
 */
public record TenantUserLimitDto(Integer maxStudents, Integer maxInstructors, Integer maxStaff, Integer maxAdmins,
                                 Integer currentStudents, Integer currentInstructors, Integer currentStaff, Integer currentAdmins,
                                 OffsetDateTime dateUpdated) {}
