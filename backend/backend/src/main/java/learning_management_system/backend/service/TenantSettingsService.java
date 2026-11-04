package learning_management_system.backend.service;

import learning_management_system.backend.dto.TenantSettingsDto;

/** tenant_settings: the caller's institution's settings (time zone, sign-in rules, MFA requirement ...). */
public interface TenantSettingsService {

    TenantSettingsDto get();

    /** PATCH (TENANT_SETTINGS_MANAGE): null values leave a setting unchanged. */
    TenantSettingsDto update(TenantSettingsDto d);
}
