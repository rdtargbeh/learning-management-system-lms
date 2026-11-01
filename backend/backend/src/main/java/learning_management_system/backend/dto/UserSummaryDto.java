package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.UserType;

/** A user account with the main name fields from user_profile. */
public record UserSummaryDto(UUID userId, UUID tenantId, boolean systemUser, UserType userType, String username, String email,
                             AccountStatus accountStatus, String displayName, String firstName, String lastName,
                             OffsetDateTime lastLogin, OffsetDateTime dateCreated) {}
