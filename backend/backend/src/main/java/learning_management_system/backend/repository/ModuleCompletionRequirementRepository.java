package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ModuleCompletionRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleCompletionRequirementRepository extends JpaRepository<ModuleCompletionRequirement, UUID> {

    List<ModuleCompletionRequirement> findByModuleIdInOrderByDisplayOrderAsc(Collection<UUID> moduleIds);

    List<ModuleCompletionRequirement> findByModuleIdOrderByDisplayOrderAsc(UUID moduleId);

    Optional<ModuleCompletionRequirement> findByModuleIdAndContentId(UUID moduleId, UUID contentId);

    /** A content item leaving a module takes its requirement with it. */
    @Modifying(flushAutomatically = true)
    @Query("delete from ModuleCompletionRequirement r where r.contentId = :contentId and r.moduleId <> :keepModuleId")
    int deleteForContentOutsideModule(@Param("contentId") UUID contentId, @Param("keepModuleId") UUID keepModuleId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ModuleCompletionRequirement r where r.contentId = :contentId")
    int deleteByContentId(@Param("contentId") UUID contentId);
}
