package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AssignmentPeerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentPeerReviewRepository extends JpaRepository<AssignmentPeerReview, UUID> {

    List<AssignmentPeerReview> findByAssignmentIdOrderByDateAssignedAsc(UUID assignmentId);

    List<AssignmentPeerReview> findByAssignmentIdAndReviewerIdOrderByDateAssignedAsc(UUID assignmentId, UUID reviewerId);

    List<AssignmentPeerReview> findBySubmissionIdIn(Collection<UUID> submissionIds);

    boolean existsByReviewerIdAndSubmissionId(UUID reviewerId, UUID submissionId);
}
