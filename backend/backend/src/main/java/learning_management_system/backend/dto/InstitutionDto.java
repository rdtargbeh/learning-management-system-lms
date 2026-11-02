package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** The institution behind a tenant (table institution, one per tenant). */
public record InstitutionDto(UUID institutionId, UUID tenantId, String institutionName, String institutionCode, String country,
                             Integer establishedYear, String institutionType, String websiteUrl, String logoUrl,
                             OffsetDateTime dateCreated, OffsetDateTime dateUpdated) {}
