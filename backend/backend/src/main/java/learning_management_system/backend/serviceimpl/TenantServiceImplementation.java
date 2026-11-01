package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.entity.TenantSettings;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSettingsRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.TenantService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The caller's own institution. */
@Service
public class TenantServiceImplementation implements TenantService {
    private final TenantRepository tenants;
    private final TenantSettingsRepository settings;
    private final TenantMapper tenantMapper;

    public TenantServiceImplementation(TenantRepository tenants, TenantSettingsRepository settings, TenantMapper tenantMapper) {
        this.tenants = tenants;
        this.settings = settings;
        this.tenantMapper = tenantMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public TenantDto current() {
        Access.requireTenant();
        return tenantMapper.toDto(tenants.findById(Access.ctx().tenantId()).orElseThrow(() -> ApiException.notFound("Tenant")));
    }

    @Transactional(readOnly = true)
    @Override
    public TenantSettingsDto settings() {
        Access.requireTenant();
        return tenantMapper.toDto(load());
    }

    @Transactional
    @Override
    public TenantSettingsDto updateSettings(TenantSettingsDto d) {
        Access.requireTenantPermission(Permissions.TENANT_SETTINGS_MANAGE);
        TenantSettings s = load();
        tenantMapper.updateEntity(d, s);
        return tenantMapper.toDto(settings.save(s));
    }

    private TenantSettings load() {
        return settings.findByTenantId(Access.ctx().tenantId()).orElseGet(() -> {
            TenantSettings s = new TenantSettings();
            s.setTenantId(Access.ctx().tenantId());
            return settings.saveAndFlush(s);
        });
    }
}
