package learning_management_system.backend.service;

import learning_management_system.backend.dto.InstitutionDto;
import learning_management_system.backend.dto.UpdateInstitutionRequest;

import java.util.UUID;

/** institution: the organisation behind a tenant (name, code, country, type, website, logo). One per tenant. */
public interface InstitutionService {

    /** Any signed-in member reads their own institution (tenantId null); platform admins read any institution. */
    InstitutionDto get(UUID tenantId);

    /**
     * PATCH (TENANT_SETTINGS_MANAGE for the own institution; platform admins for any). Creates the record when the
     * tenant has none yet; then institutionName is required. Only platform admins change institutionCode.
     */
    InstitutionDto update(UUID tenantId, UpdateInstitutionRequest r);
}
