package learning_management_system.backend.repository;

import learning_management_system.backend.entity.TenantDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantDomainRepository extends JpaRepository<TenantDomain, UUID> {
    Optional<TenantDomain> findByDomainNameIgnoreCase(String domainName);

    List<TenantDomain> findByTenantIdOrderByIsPrimaryDescDomainNameAsc(UUID tenantId);

    Optional<TenantDomain> findByDomainIdAndTenantId(UUID domainId, UUID tenantId);

    /** Clears the primary flag before another domain becomes primary (only one primary per tenant is allowed). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update TenantDomain d set d.isPrimary = false where d.tenantId = :tenantId and d.isPrimary = true")
    int clearPrimary(@Param("tenantId") UUID tenantId);
}
