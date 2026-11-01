package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ModulePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModulePrerequisiteRepository extends JpaRepository<ModulePrerequisite, UUID> {

    List<ModulePrerequisite> findByModuleIdIn(Collection<UUID> moduleIds);

    List<ModulePrerequisite> findByModuleId(UUID moduleId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ModulePrerequisite p where p.moduleId = :moduleId")
    int deleteByModuleId(@Param("moduleId") UUID moduleId);
}
