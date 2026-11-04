package learning_management_system.backend.repository;

import learning_management_system.backend.entity.ReleaseCondition;
import learning_management_system.backend.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReleaseConditionRepository extends JpaRepository<ReleaseCondition, UUID> {
    List<ReleaseCondition> findByCourseIdOrderByTargetTypeAscConditionGroupAscDateCreatedAsc(UUID courseId);

    List<ReleaseCondition> findByTargetTypeAndTargetIdOrderByConditionGroupAscDateCreatedAsc(TargetType targetType, UUID targetId);

    Optional<ReleaseCondition> findByConditionIdAndCourseId(UUID conditionId, UUID courseId);
}
