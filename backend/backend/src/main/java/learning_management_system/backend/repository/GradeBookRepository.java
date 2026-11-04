package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Gradebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradebookRepository extends JpaRepository<Gradebook, UUID> {
    Optional<Gradebook> findBySectionIdAndUserId(UUID sectionId, UUID userId);
}
