package learning_management_system.backend.security;

import java.util.UUID;

/** Entities that carry a tenant_id column. The tenant is filled from the request context on insert. */
public interface TenantScoped {
    UUID getTenantId();
    void setTenantId(UUID tenantId);
}
