package learning_management_system.backend.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import learning_management_system.backend.dto.AuditLogDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.entity.AuditLog;
import learning_management_system.backend.enums.ActorType;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.AuditLogRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.RequestContext;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import learning_management_system.backend.specification.AuditLogSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuditServiceImplementation implements AuditService {
    private final AuditLogRepository auditLog;
    private final IdentityMapper identityMapper;
    private final ObjectMapper json;
    private final SystemTx sys;

    public AuditServiceImplementation(AuditLogRepository auditLog, IdentityMapper identityMapper, ObjectMapper json, SystemTx sys) {
        this.auditLog = auditLog;
        this.identityMapper = identityMapper;
        this.json = json;
        this.sys = sys;
    }

    @Override
    public void record(String actionType, String resourceType, UUID resourceId, UUID affectedUserId, Severity severity,
                       Object oldValues, Object newValues) {
        RequestContext c = RequestContext.current();
        AuditLog a = base(c.tenantId(), c.userId(), actionType, resourceType, resourceId, severity);
        a.setActorType(c.supportAccessId() != null ? ActorType.IMPERSONATION
                : c.userId() == null ? ActorType.SYSTEM : c.isPlatformUser() ? ActorType.PLATFORM_USER : ActorType.USER);
        a.setSupportAccessId(c.supportAccessId());
        a.setAffectedUserId(affectedUserId);
        if (oldValues != null) a.setOldValues(json.valueToTree(oldValues));
        if (newValues != null) a.setNewValues(json.valueToTree(newValues));
        if (c.tenantId() == null && !c.platformAdmin()) sys.platform(() -> auditLog.save(a));   // platform staff without admin rights
        else auditLog.save(a);
    }

    @Override
    public void recordFor(UUID tenantId, UUID actorUserId, String actionType, String resourceType, UUID resourceId, Severity severity) {
        AuditLog a = base(tenantId, actorUserId, actionType, resourceType, resourceId, severity);
        a.setActorType(tenantId == null ? ActorType.PLATFORM_USER : ActorType.USER);
        a.setAffectedUserId(actorUserId);
        if (tenantId == null) sys.platform(() -> auditLog.save(a));
        else sys.inTenant(tenantId, () -> auditLog.save(a));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AuditLogDto> search(UUID tenantId, UUID actorUserId, UUID affectedUserId, String actionType, String resourceType,
                                            UUID resourceId, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        if (Access.ctx().platformAdmin()) {
            // platform admins see every tenant; tenantId narrows it down
        } else {
            Access.requireTenantPermission(Permissions.AUDIT_READ);
            tenantId = Access.ctx().tenantId();
        }
        if (from != null && to != null && !to.isAfter(from)) throw ApiException.badRequest("INVALID_RANGE", "to must be after from");
        return PageResponse.of(auditLog.findAll(AuditLogSpecification.matching(tenantId, actorUserId, affectedUserId, blank(actionType),
                blank(resourceType), resourceId, from, to), PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "dateCreated"))), identityMapper::toDto);
    }

    private AuditLog base(UUID tenantId, UUID actor, String actionType, String resourceType, UUID resourceId, Severity severity) {
        AuditLog a = new AuditLog();
        a.setTenantId(tenantId);
        a.setActorUserId(actor);
        a.setActionType(actionType);
        a.setResourceType(resourceType);
        a.setResourceId(resourceId);
        a.setActionStatus("SUCCESS");
        a.setSeverity(severity == null ? Severity.INFO : severity);
        HttpServletRequest http = currentRequest();
        if (http != null) {
            a.setIpAddress(ip(http.getRemoteAddr()));
            String ua = http.getHeader("User-Agent");
            a.setUserAgent(ua == null ? null : ua.length() > 1000 ? ua.substring(0, 1000) : ua);
        }
        return a;
    }

    static HttpServletRequest currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a ? a.getRequest() : null;
    }

    static InetAddress ip(String address) {
        try {
            return address == null ? null : InetAddress.getByName(address);
        } catch (Exception e) {
            return null;
        }
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
