package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AuditLogDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.service.AuditService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Audit log search: an institution's own trail (AUDIT_READ), or every institution's for platform admins. */
@RestController
@RequestMapping("/api/v1/audit-log")
public class AuditController {
    private final AuditService audit;

    public AuditController(AuditService audit) { this.audit = audit; }

    /** tenantId is only for platform admins. from/to are ISO date-times. */
    @GetMapping
    public PageResponse<AuditLogDto> search(@RequestParam(required = false) UUID tenantId,
                                            @RequestParam(required = false) UUID actorUserId,
                                            @RequestParam(required = false) UUID affectedUserId,
                                            @RequestParam(required = false) String actionType,
                                            @RequestParam(required = false) String resourceType,
                                            @RequestParam(required = false) UUID resourceId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return audit.search(tenantId, actorUserId, affectedUserId, actionType, resourceType, resourceId, from, to, page, size);
    }
}
