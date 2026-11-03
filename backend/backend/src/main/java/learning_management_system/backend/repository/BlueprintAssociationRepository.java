package learning_management_system.backend.repository;

import learning_management_system.backend.entity.BlueprintAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlueprintAssociationRepository extends JpaRepository<BlueprintAssociation, UUID> {
    List<BlueprintAssociation> findByBlueprintCourseIdOrderByDateCreatedAsc(UUID blueprintCourseId);

    List<BlueprintAssociation> findByBlueprintCourseIdAndIsActiveTrue(UUID blueprintCourseId);

    Optional<BlueprintAssociation> findFirstByAssociatedCourseIdAndIsActiveTrue(UUID associatedCourseId);

    Optional<BlueprintAssociation> findByBlueprintCourseIdAndAssociatedCourseId(UUID blueprintCourseId, UUID associatedCourseId);

    Optional<BlueprintAssociation> findByBlueprintAssociationIdAndBlueprintCourseId(UUID id, UUID blueprintCourseId);

    boolean existsByBlueprintCourseIdAndIsActiveTrue(UUID blueprintCourseId);
}
