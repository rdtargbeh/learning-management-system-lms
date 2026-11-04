package learning_management_system.backend.repository;

import learning_management_system.backend.entity.TenantBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** One row per tenant (tenant_id is unique). */
@Repository
public interface TenantBrandingRepository extends JpaRepository<TenantBranding, UUID> {

    Optional<TenantBranding> findByTenantId(UUID tenantId);
}
