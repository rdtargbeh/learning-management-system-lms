package learning_management_system.backend.repository;

import learning_management_system.backend.entity.CoursePaceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoursePaceItemRepository extends JpaRepository<CoursePaceItem, UUID> {
    List<CoursePaceItem> findByPaceIdOrderByDisplayOrderAsc(UUID paceId);
}
