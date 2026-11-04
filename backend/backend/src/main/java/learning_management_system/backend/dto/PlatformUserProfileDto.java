package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.PlatformRole;

/** Platform staff details (table platform_user_profile). dateLastAccess is read-only. */
public record PlatformUserProfileDto(PlatformRole platformRole, String jobTitle, String department,
                                     String employeeNumber, Boolean canImpersonate, UUID[] supportTenantScope, Boolean isActive,
                                     OffsetDateTime dateLastAccess) {}
