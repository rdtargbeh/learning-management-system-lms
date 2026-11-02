package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** An extra sign-in name of a user, e.g. a SIS login or an SSO identifier (table user_login). */
public record UserLoginDto(UUID loginId, String uniqueLogin, UUID ssoConfigId, String sisSourceId, String integrationId, Boolean isActive,
                           OffsetDateTime dateLastLogin) {}
