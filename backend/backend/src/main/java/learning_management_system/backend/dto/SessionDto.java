package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A signed-in device (table user_session). current = the session of this request. */
public record SessionDto(UUID sessionId, String ipAddress, String userAgent, OffsetDateTime lastActivity, OffsetDateTime dateCreated,
                         OffsetDateTime dateExpired, boolean current) {}
