package learning_management_system.backend.security;

import jakarta.persistence.PrePersist;

/**
 * Sets tenant_id on new rows from the current request when the caller did not set it.
 * The database still enforces the tenant with row-level security, so a wrong value is rejected.
 */
public class TenantEntityListener {
    @PrePersist
    public void assignTenant(Object entity) {
        if (entity instanceof TenantScoped scoped && scoped.getTenantId() == null) {
            scoped.setTenantId(RequestContext.current().tenantId());
        }
    }
}
