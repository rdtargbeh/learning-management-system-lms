package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.dto.InitialAdminRequest;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.UpdateTenantRequest;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.entity.Institution;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.TenantDomain;
import learning_management_system.backend.entity.TenantSettings;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AuthProvider;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.InstitutionRepository;
import learning_management_system.backend.repository.TenantDomainRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSettingsRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.TenantService;
import learning_management_system.backend.service.UserProvisioningService;
import learning_management_system.backend.service.UserService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static learning_management_system.backend.utility.Patch.set;

@Service
public class TenantServiceImplementation implements TenantService {
    private final TenantRepository tenants;
    private final TenantSettingsRepository settings;
    private final TenantDomainRepository domains;
    private final InstitutionRepository institutions;
    private final UserProvisioningService provisioning;
    private final UserService userService;
    private final TenantMapper tenantMapper;
    private final AuditLogService audit;

    public TenantServiceImplementation(TenantRepository tenants, TenantSettingsRepository settings, TenantDomainRepository domains,
                                       InstitutionRepository institutions, UserProvisioningService provisioning,
                                       UserService userService, TenantMapper tenantMapper, AuditLogService audit) {
        this.tenants = tenants;
        this.settings = settings;
        this.domains = domains;
        this.institutions = institutions;
        this.provisioning = provisioning;
        this.userService = userService;
        this.tenantMapper = tenantMapper;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public TenantDto current() {
        Access.requireTenant();
        return tenantMapper.toDto(tenants.findById(Access.ctx().tenantId()).orElseThrow(() -> ApiException.notFound("Tenant")));
    }

    @Transactional
    @Override
    public CreateTenantResponse create(CreateTenantRequest r) {
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
            String name = r.primaryDomain().trim().toLowerCase();
            if (domains.findByDomainNameIgnoreCase(name).isPresent()) throw ApiException.conflict("DOMAIN_TAKEN", "This domain is already registered");
            TenantDomain d = new TenantDomain();
            d.setTenantId(tid);
            d.setDomainName(name);
            d.setIsPrimary(true);
            d.setIsVerified(true);                       // given by a platform admin during onboarding
            d.setDateVerified(OffsetDateTime.now());
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
        audit.record("TENANT_CREATED", "TENANT", tid, null, Severity.INFO, null, Map.of("tenantCode", t.getTenantCode()));
        return new CreateTenantResponse(tenantMapper.toDto(t), userService.toSummary(admin));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<TenantDto> list(int page, int size) {
        Access.requirePlatformAdmin();
        return PageResponse.of(tenants.findAll(PageRequest.of(page, Math.min(size, 200), Sort.by("tenantName"))), tenantMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public TenantDto get(UUID tenantId) {
        Access.requirePlatformAdmin();
        return tenantMapper.toDto(tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant")));
    }

    @Transactional
    @Override
    public TenantDto update(UUID tenantId, UpdateTenantRequest r) {
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
            audit.record("TENANT_STATUS_CHANGED", "TENANT", tenantId, null, Severity.WARNING, null, Map.of("tenantStatus", r.tenantStatus()));
        }
        return tenantMapper.toDto(tenants.save(t));
    }
}
