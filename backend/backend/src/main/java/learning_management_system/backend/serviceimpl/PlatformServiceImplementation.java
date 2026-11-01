package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.dto.InitialAdminRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.SupportAccessRequest;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.UpdateTenantRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.AuditLog;
import learning_management_system.backend.entity.Institution;
import learning_management_system.backend.entity.PlatformSupportAccess;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.TenantDomain;
import learning_management_system.backend.entity.TenantSettings;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccessStatus;
import learning_management_system.backend.enums.ActorType;
import learning_management_system.backend.enums.AuthProvider;
import learning_management_system.backend.enums.PlatformRole;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.PlatformSupportAccessMapper;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.AuditLogRepository;
import learning_management_system.backend.repository.InstitutionRepository;
import learning_management_system.backend.repository.PlatformSupportAccessRepository;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.TenantDomainRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSettingsRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.PlatformService;
import learning_management_system.backend.service.UserProvisioningService;
import learning_management_system.backend.service.UserService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static learning_management_system.backend.utility.Patch.set;

/** Platform-operator features: onboarding institutions, platform staff, and audited support access into a tenant. */
@Service
public class PlatformServiceImplementation implements PlatformService {
    private static final Set<PlatformRole> APPROVER_ROLES =
            Set.of(PlatformRole.SUPER_ADMIN, PlatformRole.PLATFORM_ADMIN);

    private final TenantRepository tenants;
    private final TenantSettingsRepository settings;
    private final TenantDomainRepository domains;
    private final InstitutionRepository institutions;
    private final UserRepository users;
    private final PlatformUserProfileRepository platformProfiles;
    private final PlatformSupportAccessRepository supportAccess;
    private final AuditLogRepository auditLog;
    private final UserProvisioningService provisioning;
    private final AuthService auth;
    private final SystemTx sys;
    private final UserService userService;
    private final TenantMapper tenantMapper;
    private final UserMapper userMapper;
    private final PlatformSupportAccessMapper supportAccessMapper;

    public PlatformServiceImplementation(TenantRepository tenants, TenantSettingsRepository settings, TenantDomainRepository domains,
                                         InstitutionRepository institutions, UserRepository users,
                                         PlatformUserProfileRepository platformProfiles,
                                         PlatformSupportAccessRepository supportAccess, AuditLogRepository auditLog,
                                         UserProvisioningService provisioning, AuthService auth, SystemTx sys,
                                         UserService userService, TenantMapper tenantMapper, UserMapper userMapper,
                                         PlatformSupportAccessMapper supportAccessMapper) {
        this.tenants = tenants;
        this.settings = settings;
        this.domains = domains;
        this.institutions = institutions;
        this.users = users;
        this.platformProfiles = platformProfiles;
        this.supportAccess = supportAccess;
        this.auditLog = auditLog;
        this.provisioning = provisioning;
        this.auth = auth;
        this.sys = sys;
        this.userService = userService;
        this.tenantMapper = tenantMapper;
        this.userMapper = userMapper;
        this.supportAccessMapper = supportAccessMapper;
    }

    // ---------------------------------------------------------------- tenants

    @Transactional
    @Override
    public CreateTenantResponse createTenant(CreateTenantRequest r) {
        Access.requirePlatformAdmin();
        tenants.findByTenantCodeIgnoreCase(r.tenantCode()).ifPresent(t -> {
            throw ApiException.conflict("TENANT_CODE_TAKEN", "Tenant code already in use");
        });
        Tenant t = new Tenant();
        t.setTenantName(r.tenantName().trim());
        t.setTenantCode(r.tenantCode().trim().toUpperCase());
        t.setTenantDescription(r.tenantDescription());
        t.setDataRegion(r.dataRegion());
        set(r.defaultTimezone(), t::setDefaultTimezone);
        set(r.defaultLocale(), t::setDefaultLocale);
        t.setCreatedBy(Access.ctx().userId());
        tenants.saveAndFlush(t);
        UUID tid = t.getTenantId();

        TenantSettings s = new TenantSettings();
        s.setTenantId(tid);
        settings.save(s);
        if (r.primaryDomain() != null && !r.primaryDomain().isBlank()) {
            TenantDomain d = new TenantDomain();
            d.setTenantId(tid);
            d.setDomainName(r.primaryDomain().trim().toLowerCase());
            d.setIsPrimary(true);
            domains.save(d);
        }
        if (r.institutionName() != null && !r.institutionName().isBlank()) {
            Institution i = new Institution();
            i.setTenantId(tid);
            i.setInstitutionName(r.institutionName().trim());
            i.setInstitutionCode(t.getTenantCode());
            institutions.save(i);
        }
        InitialAdminRequest a = r.admin();
        User admin = provisioning.provisionTenantUser(new CreateUserRequest(a.username(), a.email(), UserType.TENANT_ADMIN,
                a.password(), AuthProvider.LOCAL, null,
                new UserProfileDto(a.firstName(), null, a.lastName(), null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null)), tid);
        return new CreateTenantResponse(tenantMapper.toDto(t), userService.toSummary(admin));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<TenantDto> listTenants(int page, int size) {
        Access.requirePlatformAdmin();
        return PageResponse.of(tenants.findAll(PageRequest.of(page, Math.min(size, 200), Sort.by("tenantName"))), tenantMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public TenantDto getTenant(UUID tenantId) {
        Access.requirePlatformAdmin();
        return tenantMapper.toDto(tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant")));
    }

    @Transactional
    @Override
    public TenantDto updateTenant(UUID tenantId, UpdateTenantRequest r) {
        Access.requirePlatformAdmin();
        Tenant t = tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant"));
        set(r.tenantName(), t::setTenantName);
        set(r.tenantDescription(), t::setTenantDescription);
        set(r.dataRegion(), t::setDataRegion);
        set(r.defaultTimezone(), t::setDefaultTimezone);
        set(r.defaultLocale(), t::setDefaultLocale);
        if (r.tenantStatus() != null) {
            t.setTenantStatus(r.tenantStatus());
            t.setIsActive(r.tenantStatus() == TenantStatus.ACTIVE);
            t.setSuspendedAt(r.tenantStatus() == TenantStatus.SUSPENDED ? OffsetDateTime.now() : null);
        }
        return tenantMapper.toDto(tenants.save(t));
    }

    // ---------------------------------------------------------------- platform staff

    @Transactional
    @Override
    public UserDetailDto createPlatformUser(CreatePlatformUserRequest r) {
        Access.requirePlatformAdmin();
        boolean callerIsSuper = sys.platform(() -> platformProfiles.findByUserId(Access.ctx().userId())
                .map(p -> p.getPlatformRole() == PlatformRole.SUPER_ADMIN).orElse(false));
        if (r.platformRole() == PlatformRole.SUPER_ADMIN && !callerIsSuper) {
            throw ApiException.forbidden("Only a super admin can create another super admin");
        }
        User u = provisioning.provisionPlatformUser(r, Access.ctx().userId());
        return userService.toDetail(u);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryDto> listPlatformUsers() {
        Access.requirePlatformAdmin();
        return userService.toSummaries(users.findByIsSystemUserTrueOrderByUsername());
    }

    @Transactional
    @Override
    public PlatformUserProfileDto updatePlatformProfile(UUID userId, PlatformUserProfileDto d) {
        Access.requirePlatformAdmin();
        PlatformUserProfile p = platformProfiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Platform user"));
        if (d.platformRole() == PlatformRole.SUPER_ADMIN) {
            boolean callerIsSuper = platformProfiles.findByUserId(Access.ctx().userId())
                    .map(x -> x.getPlatformRole() == PlatformRole.SUPER_ADMIN).orElse(false);
            if (!callerIsSuper) throw ApiException.forbidden("Only a super admin can grant super admin");
        }
        userMapper.updateEntity(d, p);
        return userMapper.toDto(platformProfiles.save(p));
    }

    // ---------------------------------------------------------------- support access ("act in tenant")
    // Bookkeeping runs in a trusted system context: a support user is not a platform admin and cannot see the tenant yet.

    @Override
    public PlatformSupportAccessDto requestSupportAccess(SupportAccessRequest r) {
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
    public PlatformSupportAccessDto approveSupportAccess(UUID id, boolean approve) {
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
    public LoginResponse startSupportSession(UUID id) {
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
        return new LoginResponse(token, "Bearer", auth.ttlSeconds(), summary, List.of(), null, started.getSupportAccessId());
    }

    @Override
    public PlatformSupportAccessDto endSupportSession(UUID id) {
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
    public List<PlatformSupportAccessDto> mySupportAccess() {
        Access.requirePlatformUser();
        UUID me = Access.ctx().userId();
        boolean admin = Access.ctx().platformAdmin();
        return sys.platform(() -> (admin ? supportAccess.findAll() : supportAccess.findByPlatformUserIdOrderByDateCreatedDesc(me))
                .stream().map(supportAccessMapper::toDto).toList());
    }
}
