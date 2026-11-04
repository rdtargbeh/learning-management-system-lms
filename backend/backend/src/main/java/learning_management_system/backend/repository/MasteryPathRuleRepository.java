package learning_management_system.backend.repository;

import learning_management_system.backend.entity.MasteryPathRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MasteryPathRuleRepository extends JpaRepository<MasteryPathRule, UUID> {
    List<MasteryPathRule> findByCourseIdOrderByDateCreatedAsc(UUID courseId);

    boolean existsByTriggerAssignmentId(UUID triggerAssignmentId);
}
