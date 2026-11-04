package learning_management_system.backend.repository;

import learning_management_system.backend.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    List<Feedback> findBySubmissionIdInOrderByDateGradedDesc(Collection<UUID> submissionIds);

    Optional<Feedback> findFirstBySubmissionIdOrderByDateGradedDesc(UUID submissionId);

    Optional<Feedback> findFirstBySubmissionIdAndEvaluatorId(UUID submissionId, UUID evaluatorId);
}
