package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.entity.TenantSettings;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.TenantSettingsRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.TenantSettingsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** tenant_settings of the caller's institution; the row is created on first use if onboarding did not make it. */
@Service
public class TenantSettingsServiceImplementation implements TenantSettingsService {
    private final TenantSettingsRepository settings;
    private final TenantMapper tenantMapper;

    public TenantSettingsServiceImplementation(TenantSettingsRepository settings, TenantMapper tenantMapper) {
        this.settings = settings;
        this.tenantMapper = tenantMapper;
    }

    @Transactional
    @Override
    public TenantSettingsDto get() {
        Access.requireTenant();
        return tenantMapper.toDto(load());
    }

    @Transactional
    @Override
    public TenantSettingsDto update(TenantSettingsDto d) {
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
