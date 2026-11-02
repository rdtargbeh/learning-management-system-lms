package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateTenantRequest;
import learning_management_system.backend.dto.CreateTenantResponse;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.UpdateTenantRequest;

import java.util.UUID;

/** tenant: institutions on the platform. Platform admins onboard and manage them; members read their own. */
public interface TenantService {

    /** The caller's own institution. */
    TenantDto current();

    /** Onboards an institution: tenant, default settings, optional institution record and primary domain, first admin. */
    CreateTenantResponse create(CreateTenantRequest r);

    PageResponse<TenantDto> list(int page, int size);

    TenantDto get(UUID tenantId);

    /** PATCH; a status change also sets is_active and the suspension time. */
    TenantDto update(UUID tenantId, UpdateTenantRequest r);
}
