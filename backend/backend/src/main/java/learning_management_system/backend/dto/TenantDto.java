package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.TenantStatus;

/** An institution (table tenant). */
public record TenantDto(UUID tenantId, String tenantName, String tenantCode, String tenantDescription, TenantStatus tenantStatus,
                        Boolean isActive, String dataRegion, String defaultTimezone, String defaultLocale, OffsetDateTime dateCreated) {}
