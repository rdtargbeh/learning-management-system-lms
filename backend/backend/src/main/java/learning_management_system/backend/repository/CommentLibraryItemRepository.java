package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CommentLibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentLibraryItemRepository extends JpaRepository<CommentLibraryItem, UUID> {

    List<CommentLibraryItem> findByUserIdOrderByUsageCountDescDateCreatedDesc(UUID userId);
}
