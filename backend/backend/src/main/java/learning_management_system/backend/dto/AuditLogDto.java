package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ActorType;
import learning_management_system.backend.enums.Severity;

/** One audit entry (table audit_log). */
public record AuditLogDto(UUID auditLogId, UUID tenantId, UUID actorUserId, ActorType actorType, UUID supportAccessId,
                          UUID affectedUserId, String actionType, String resourceType, UUID resourceId, String actionStatus,
                          Severity severity, JsonNode oldValues, JsonNode newValues, String ipAddress, String userAgent,
                          OffsetDateTime dateCreated) {}
