package learning_management_system.backend.dto;

import learning_management_system.backend.enums.TenantStatus;

/** PATCH an institution; null fields are left unchanged. */
public record UpdateTenantRequest(String tenantName, String tenantDescription, TenantStatus tenantStatus,
                                  String dataRegion, String defaultTimezone, String defaultLocale) {}
