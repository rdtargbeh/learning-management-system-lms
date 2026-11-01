package learning_management_system.backend.repository;

import learning_management_system.backend.entity.StudentModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentModuleProgressRepository extends JpaRepository<StudentModuleProgress, UUID> {

    List<StudentModuleProgress> findByUserIdAndModuleIdIn(UUID userId, Collection<UUID> moduleIds);

    List<StudentModuleProgress> findByModuleId(UUID moduleId);
}
