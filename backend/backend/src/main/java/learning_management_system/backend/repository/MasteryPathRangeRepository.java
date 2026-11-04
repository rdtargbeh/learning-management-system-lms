package learning_management_system.backend.repository;

import learning_management_system.backend.entity.MasteryPathRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MasteryPathRangeRepository extends JpaRepository<MasteryPathRange, UUID> {
    List<MasteryPathRange> findByRuleIdOrderByLowerBoundPercentAsc(UUID ruleId);

    List<MasteryPathRange> findByRuleIdIn(Collection<UUID> ruleIds);
}
