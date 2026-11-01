package learning_management_system.backend.service;

import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;

/** The caller's own institution and its settings. */
public interface TenantService {

    TenantDto current();

    TenantSettingsDto settings();

    TenantSettingsDto updateSettings(TenantSettingsDto d);
}
