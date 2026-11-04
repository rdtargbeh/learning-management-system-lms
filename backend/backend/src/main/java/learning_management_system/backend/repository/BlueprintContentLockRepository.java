package learning_management_system.backend.repository;

import learning_management_system.backend.entity.BlueprintContentLock;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlueprintContentLockRepository extends JpaRepository<BlueprintContentLock, UUID> {
    List<BlueprintContentLock> findByBlueprintCourseIdOrderByDateCreatedAsc(UUID blueprintCourseId);

    Optional<BlueprintContentLock> findByBlueprintCourseIdAndObjectTypeAndObjectId(UUID blueprintCourseId,
                                                                                  BlueprintContentLockObjectType type, UUID objectId);

    Optional<BlueprintContentLock> findByLockIdAndBlueprintCourseId(UUID lockId, UUID blueprintCourseId);
}
