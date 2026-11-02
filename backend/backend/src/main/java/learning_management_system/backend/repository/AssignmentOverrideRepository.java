package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AssignmentOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentOverrideRepository extends JpaRepository<AssignmentOverride, UUID> {

    List<AssignmentOverride> findByAssignmentIdOrderByDateCreatedAsc(UUID assignmentId);

    List<AssignmentOverride> findByAssignmentIdIn(Collection<UUID> assignmentIds);
}
