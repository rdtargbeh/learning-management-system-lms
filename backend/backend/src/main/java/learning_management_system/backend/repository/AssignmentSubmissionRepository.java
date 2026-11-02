package learning_management_system.backend.repository;

import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    List<AssignmentSubmission> findByAssignmentIdOrderByUserIdAscSubmissionNumberAsc(UUID assignmentId);

    List<AssignmentSubmission> findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(UUID assignmentId, UUID userId);

    List<AssignmentSubmission> findByUserIdAndAssignmentIdIn(UUID userId, Collection<UUID> assignmentIds);

    boolean existsByAssignmentIdAndSubmissionStatusNot(UUID assignmentId, AssignmentSubmissionStatus status);

    @Query("select coalesce(max(s.submissionNumber), 0) from AssignmentSubmission s where s.assignmentId = :a and s.userId = :u")
    int maxSubmissionNumber(@Param("a") UUID assignmentId, @Param("u") UUID userId);

    /** Grade posting: every GRADED submission of the assignment becomes RETURNED (visible to the student). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update AssignmentSubmission s
        set s.submissionStatus = learning_management_system.backend.enums.AssignmentSubmissionStatus.RETURNED
        where s.assignmentId = :a and s.submissionStatus = learning_management_system.backend.enums.AssignmentSubmissionStatus.GRADED
        """)
    int postGrades(@Param("a") UUID assignmentId);
}
