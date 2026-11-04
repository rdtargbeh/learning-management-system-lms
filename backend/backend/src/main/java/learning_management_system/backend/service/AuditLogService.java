package learning_management_system.backend.service;

import learning_management_system.backend.dto.AuditLogDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.enums.Severity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The audit trail (table audit_log). Services record security-relevant actions; institution admins (or AUDIT_READ)
 * search their institution's trail, platform admins search every institution's.
 */
public interface AuditLogService {

    /** Records an action by the current caller in the current institution. */
    void record(String actionType, String resourceType, UUID resourceId, UUID affectedUserId, Severity severity,
                Object oldValues, Object newValues);

    /** Records an action outside a signed-in request (e.g. a password reset by link): the user acts on their own account. */
    void recordFor(UUID tenantId, UUID actorUserId, String actionType, String resourceType, UUID resourceId, Severity severity);

    PageResponse<AuditLogDto> search(UUID tenantId, UUID actorUserId, UUID affectedUserId, String actionType, String resourceType,
                                     UUID resourceId, OffsetDateTime from, OffsetDateTime to, int page, int size);
}
