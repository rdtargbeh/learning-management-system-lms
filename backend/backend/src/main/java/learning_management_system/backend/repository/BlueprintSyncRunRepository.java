package learning_management_system.backend.repository;

import learning_management_system.backend.entity.BlueprintSyncRun;
import learning_management_system.backend.enums.BlueprintSyncRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlueprintSyncRunRepository extends JpaRepository<BlueprintSyncRun, UUID> {
    List<BlueprintSyncRun> findByBlueprintCourseIdOrderByDateCreatedDesc(UUID blueprintCourseId);

    Optional<BlueprintSyncRun> findBySyncRunIdAndBlueprintCourseId(UUID syncRunId, UUID blueprintCourseId);

    /** The latest finished run: its copy map says which course item came from which blueprint item. */
    Optional<BlueprintSyncRun> findFirstByBlueprintCourseIdAndRunStatusInOrderByDateCreatedDesc(UUID blueprintCourseId,
                                                                                              Collection<BlueprintSyncRunStatus> statuses);

    boolean existsByBlueprintCourseIdAndRunStatusIn(UUID blueprintCourseId, Collection<BlueprintSyncRunStatus> statuses);
}
