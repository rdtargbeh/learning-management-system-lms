package learning_management_system.backend.repository;

import learning_management_system.backend.entity.OutcomeResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutcomeResultRepository extends JpaRepository<OutcomeResult, UUID> {
    boolean existsByOutcomeId(UUID outcomeId);
}
