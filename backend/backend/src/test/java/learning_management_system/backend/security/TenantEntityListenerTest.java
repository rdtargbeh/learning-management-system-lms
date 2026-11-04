package learning_management_system.backend.security;

import learning_management_system.backend.entity.Course;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantEntityListenerTest {

    @Test
    void fillsTenantFromRequestContextOnlyWhenMissing() {
        UUID tenant = UUID.randomUUID(), explicit = UUID.randomUUID();
        RequestContext ctx = new RequestContext(tenant, UUID.randomUUID(), false, "TENANT_ADMIN", null, Set.of());
        Course fromContext = new Course();
        Course alreadySet = new Course();
        alreadySet.setTenantId(explicit);
        RequestContext.runAs(ctx, () -> {
            new TenantEntityListener().assignTenant(fromContext);
            new TenantEntityListener().assignTenant(alreadySet);
            return null;
        });
        assertThat(fromContext.getTenantId()).isEqualTo(tenant);
        assertThat(alreadySet.getTenantId()).isEqualTo(explicit);
        assertThat(RequestContext.current().tenantId()).isNull();   // context restored after runAs
    }
}
