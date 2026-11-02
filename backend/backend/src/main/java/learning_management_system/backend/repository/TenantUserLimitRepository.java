package learning_management_system.backend.repository;

import jakarta.persistence.LockModeType;
import learning_management_system.backend.entity.TenantUserLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** One row per tenant (tenant_id is unique). */
@Repository
public interface TenantUserLimitRepository extends JpaRepository<TenantUserLimit, UUID> {

    Optional<TenantUserLimit> findByTenantId(UUID tenantId);

    /** Row lock so two accounts created at once cannot both take the last seat. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from TenantUserLimit l where l.tenantId = :tenantId")
    Optional<TenantUserLimit> lockByTenantId(@Param("tenantId") UUID tenantId);
}
