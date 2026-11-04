package learning_management_system.backend.dto;

/** Institution settings (table tenant_settings). As a PATCH body, null fields are left unchanged. */
public record TenantSettingsDto(String themeColor, String bannerLogoUrl, String supportEmail, String fromEmail, Integer maxUploadSizeMb,
                                Integer sessionTimeoutMinutes, Integer passwordMinLength, Boolean requireMfa,
                                Boolean allowSelfRegistration, Boolean maintenanceMode) {}
