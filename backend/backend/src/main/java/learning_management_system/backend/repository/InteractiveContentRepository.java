package learning_management_system.backend.repository;

import learning_management_system.backend.entity.InteractiveContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InteractiveContentRepository extends JpaRepository<InteractiveContent, UUID> {
    Optional<InteractiveContent> findByContentId(UUID contentId);
}
