package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.PublicBrandingDto;
import learning_management_system.backend.dto.TenantBrandingDto;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.TenantBranding;
import learning_management_system.backend.enums.TenantStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.TenantBrandingRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.TenantBrandingService;
import learning_management_system.backend.service.TenantDomainService;

import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantBrandingServiceImplementation implements TenantBrandingService {
    private final TenantBrandingRepository branding;
    private final TenantRepository tenants;
    private final TenantMapper tenantMapper;
    private final SystemTx sys;
    private final TenantDomainService domains;

    public TenantBrandingServiceImplementation(TenantBrandingRepository branding, TenantRepository tenants, TenantMapper tenantMapper,
                                               SystemTx sys, TenantDomainService domains) {
        this.domains = domains;
        this.branding = branding;
        this.tenants = tenants;
        this.tenantMapper = tenantMapper;
        this.sys = sys;
    }

    @Transactional(readOnly = true)
    @Override
    public TenantBrandingDto get() {
        Access.requireTenant();
        return branding.findByTenantId(Access.ctx().tenantId()).map(tenantMapper::toDto)
                .orElse(new TenantBrandingDto(null, null, null, null, null, null, null, null));
    }

    @Transactional
    @Override
    public TenantBrandingDto replace(TenantBrandingDto d) {
        Access.requireTenantPermission(Permissions.TENANT_SETTINGS_MANAGE);
        TenantBranding b = branding.findByTenantId(Access.ctx().tenantId()).orElseGet(() -> {
            TenantBranding n = new TenantBranding();
            n.setTenantId(Access.ctx().tenantId());
            return n;
        });
        tenantMapper.replaceEntity(d, b);
        return tenantMapper.toDto(branding.saveAndFlush(b));
    }

    /** Runs in a trusted context: the caller has no tenant yet. Suspended or unknown institutions answer 404. */
    @Override
    public PublicBrandingDto publicBranding(String tenantCode) {
        return sys.platform(() -> of(tenants.findByTenantCodeIgnoreCase(tenantCode.trim()).orElse(null)));
    }

    @Override
    public PublicBrandingDto publicBrandingByDomain(String domainName) {
        UUID tenantId = domains.tenantForDomain(domainName);
        if (tenantId == null) throw ApiException.notFound("Institution");
        return sys.platform(() -> of(tenants.findById(tenantId).orElse(null)));
    }

    /** Suspended or unknown institutions answer 404. */
    private PublicBrandingDto of(Tenant t) {
        if (t == null || t.getTenantStatus() != TenantStatus.ACTIVE || !Boolean.TRUE.equals(t.getIsActive())) {
            throw ApiException.notFound("Institution");
        }
        return tenantMapper.toPublicDto(t, branding.findByTenantId(t.getTenantId()).orElse(new TenantBranding()));
    }
}
