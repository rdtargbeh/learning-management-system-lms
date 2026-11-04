package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.SupportAccessRequest;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.AuditLog;
import learning_management_system.backend.entity.PlatformSupportAccess;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.enums.AccessStatus;
import learning_management_system.backend.enums.ActorType;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.PlatformSupportAccessMapper;
import learning_management_system.backend.repository.AuditLogRepository;
import learning_management_system.backend.repository.PlatformSupportAccessRepository;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.PlatformSupportAccessService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * platform_support_access: platform staff enter an institution only through an approved, time-boxed session.
 * Bookkeeping runs in a trusted system context: a support user is not a platform admin and cannot see the tenant yet.
 */
@Service
public class PlatformSupportAccessServiceImplementation implements PlatformSupportAccessService {
    private final PlatformSupportAccessRepository supportAccess;
    private final PlatformUserProfileRepository platformProfiles;
    private final TenantRepository tenants;
    private final UserRepository users;
    private final AuditLogRepository auditLog;
    private final AuthService auth;
    private final UserService userService;
    private final SystemTx sys;
    private final PlatformSupportAccessMapper supportAccessMapper;

    public PlatformSupportAccessServiceImplementation(PlatformSupportAccessRepository supportAccess,
                                                      PlatformUserProfileRepository platformProfiles, TenantRepository tenants,
                                                      UserRepository users, AuditLogRepository auditLog, AuthService auth,
                                                      UserService userService, SystemTx sys,
                                                      PlatformSupportAccessMapper supportAccessMapper) {
        this.supportAccess = supportAccess;
        this.platformProfiles = platformProfiles;
        this.tenants = tenants;
        this.users = users;
        this.auditLog = auditLog;
        this.auth = auth;
        this.userService = userService;
        this.sys = sys;
        this.supportAccessMapper = supportAccessMapper;
    }
    // Bookkeeping runs in a trusted system context: a support user is not a platform admin and cannot see the tenant yet.

    @Override
    public PlatformSupportAccessDto request(SupportAccessRequest r) {
        Access.requirePlatformUser();
        if (Access.ctx().tenantId() != null) throw ApiException.badRequest("IN_TENANT", "Leave the current support session first");
        UUID me = Access.ctx().userId();
        int minutes = r.durationMinutes() == null ? 60 : Math.max(5, Math.min(r.durationMinutes(), 480));
        return sys.platform(() -> {
            PlatformUserProfile profile = platformProfiles.findByUserId(me).orElseThrow(() -> ApiException.forbidden("No platform profile"));
            if (!Boolean.TRUE.equals(profile.getIsActive())) throw ApiException.forbidden("Platform profile is inactive");
            if (profile.getSupportTenantScope() != null && profile.getSupportTenantScope().length > 0
                    && !List.of(profile.getSupportTenantScope()).contains(r.tenantId())) {
                throw ApiException.forbidden("This tenant is outside your support scope");
            }
            if (r.impersonatedUserId() != null && !Boolean.TRUE.equals(profile.getCanImpersonate())) {
                throw ApiException.forbidden("You are not allowed to impersonate users");
            }
            tenants.findById(r.tenantId()).orElseThrow(() -> ApiException.notFound("Tenant"));
            PlatformSupportAccess a = new PlatformSupportAccess();
            a.setTenantId(r.tenantId());
            a.setPlatformUserId(me);
            a.setImpersonatedUserId(r.impersonatedUserId());
            a.setAccessReason(r.accessReason());
            a.setTicketReference(r.ticketReference());
            a.setDateExpires(OffsetDateTime.now().plusMinutes(minutes));
            return supportAccessMapper.toDto(supportAccess.saveAndFlush(a));
        });
    }

    /** A platform admin other than the requester approves (four-eyes rule). */
    @Override
    public PlatformSupportAccessDto decide(UUID id, boolean approve) {
        Access.requirePlatformAdmin();
        UUID me = Access.ctx().userId();
        return sys.platform(() -> {
            PlatformSupportAccess a = supportAccess.findById(id).orElseThrow(() -> ApiException.notFound("Support access"));
            if (a.getPlatformUserId().equals(me)) throw ApiException.forbidden("You cannot approve your own support access");
            if (a.getAccessStatus() != AccessStatus.REQUESTED) {
                throw ApiException.conflict("NOT_PENDING", "Request is " + a.getAccessStatus());
            }
            a.setAccessStatus(approve ? AccessStatus.APPROVED : AccessStatus.DENIED);
            a.setApprovedBy(me);
            return supportAccessMapper.toDto(supportAccess.save(a));
        });
    }

    /** Starts the session and returns a tenant-scoped token; every action is attributable through audit_log. */
    @Override
    public LoginResponse start(UUID id) {
        Access.requirePlatformUser();
        UUID me = Access.ctx().userId();
        PlatformSupportAccess started = sys.platform(() -> {
            PlatformSupportAccess a = supportAccess.findById(id).orElseThrow(() -> ApiException.notFound("Support access"));
            if (!a.getPlatformUserId().equals(me)) throw ApiException.forbidden("This support access belongs to someone else");
            if (a.getAccessStatus() != AccessStatus.APPROVED
                    && a.getAccessStatus() != AccessStatus.ACTIVE) {
                throw ApiException.conflict("NOT_APPROVED", "Support access is " + a.getAccessStatus());
            }
            if (a.getDateExpires().isBefore(OffsetDateTime.now())) {
                a.setAccessStatus(AccessStatus.EXPIRED);
                supportAccess.save(a);
                throw ApiException.conflict("EXPIRED", "Support access has expired");
            }
            a.setAccessStatus(AccessStatus.ACTIVE);
            if (a.getDateStart() == null) a.setDateStart(OffsetDateTime.now());
            supportAccess.save(a);
            AuditLog log = new AuditLog();
            log.setTenantId(a.getTenantId());
            log.setActorUserId(me);
            log.setActorType(ActorType.IMPERSONATION);
            log.setSupportAccessId(a.getSupportAccessId());
            log.setAffectedUserId(a.getImpersonatedUserId());
            log.setActionType("SUPPORT_SESSION_STARTED");
            log.setResourceType("TENANT");
            log.setResourceId(a.getTenantId());
            log.setActionStatus("SUCCESS");
            log.setSeverity(Severity.WARNING);
            auditLog.save(log);
            return a;
        });
        String token = auth.supportToken(me, started.getTenantId(), started.getSupportAccessId());
        UserSummaryDto summary = sys.platform(() -> userService.toSummary(users.findById(me).orElseThrow()));
        return new LoginResponse(token, "Bearer", auth.ttlSeconds(), null, null, null, summary, List.of(), null,
                started.getSupportAccessId(), false, null, false);
    }

    @Override
    public PlatformSupportAccessDto end(UUID id) {
        Access.requirePlatformUser();
        UUID me = Access.ctx().userId();
        boolean admin = Access.ctx().platformAdmin();
        return sys.platform(() -> {
            PlatformSupportAccess a = supportAccess.findById(id).orElseThrow(() -> ApiException.notFound("Support access"));
            if (!a.getPlatformUserId().equals(me) && !admin) throw ApiException.forbidden("Not your support session");
            a.setAccessStatus(AccessStatus.ENDED);
            a.setDateEnded(OffsetDateTime.now());
            return supportAccessMapper.toDto(supportAccess.save(a));
        });
    }

    @Override
    public List<PlatformSupportAccessDto> list() {
        Access.requirePlatformUser();
        UUID me = Access.ctx().userId();
        boolean admin = Access.ctx().platformAdmin();
        return sys.platform(() -> (admin ? supportAccess.findAll() : supportAccess.findByPlatformUserIdOrderByDateCreatedDesc(me))
                .stream().map(supportAccessMapper::toDto).toList());
    }
}
