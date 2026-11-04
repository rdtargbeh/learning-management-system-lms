package learning_management_system.backend.repository;

import learning_management_system.backend.entity.MasteryPathSetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MasteryPathSetItemRepository extends JpaRepository<MasteryPathSetItem, UUID> {
    List<MasteryPathSetItem> findByRangeIdOrderBySetNumberAscDisplayOrderAsc(UUID rangeId);

    List<MasteryPathSetItem> findByRangeIdIn(Collection<UUID> rangeIds);

    List<MasteryPathSetItem> findByContentId(UUID contentId);

    List<MasteryPathSetItem> findByAssignmentId(UUID assignmentId);
}
