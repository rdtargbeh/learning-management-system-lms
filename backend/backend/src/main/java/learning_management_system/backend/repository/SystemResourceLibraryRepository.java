package learning_management_system.backend.repository;

import learning_management_system.backend.entity.SystemResourceLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Row-level security lets an institution read its own rows and the platform's (tenant_id NULL) rows. */
@Repository
public interface SystemResourceLibraryRepository extends JpaRepository<SystemResourceLibrary, UUID> {
    List<SystemResourceLibrary> findAllByOrderByResourceNameAsc();

    List<SystemResourceLibrary> findByTenantIdIsNullOrderByResourceNameAsc();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SystemResourceLibrary r set r.usageCount = coalesce(r.usageCount, 0) + 1 where r.resourceId = :id")
    int countUse(@Param("id") UUID id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SystemResourceLibrary r set r.downloadCount = coalesce(r.downloadCount, 0) + 1 where r.resourceId = :id")
    int countDownload(@Param("id") UUID id);
}
