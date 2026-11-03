package learning_management_system.backend.utility;

import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.entity.Feedback;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.ProgressStatus;
import learning_management_system.backend.enums.StudentContentProgressStatus;
import learning_management_system.backend.repository.*;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** What a student has done, for release conditions and mastery paths: views, completions, submissions, posted scores. */
@Component
public class StudentWorkLookup {
    private static final Set<AssignmentSubmissionStatus> HANDED_IN =
            EnumSet.of(AssignmentSubmissionStatus.SUBMITTED, AssignmentSubmissionStatus.GRADED, AssignmentSubmissionStatus.RETURNED);

    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final FeedbackRepository feedback;
    private final StudentContentProgressRepository contentProgress;
    private final StudentModuleProgressRepository moduleProgress;

    public StudentWorkLookup(AssignmentRepository assignments, AssignmentSubmissionRepository submissions, FeedbackRepository feedback,
                             StudentContentProgressRepository contentProgress, StudentModuleProgressRepository moduleProgress) {
        this.assignments = assignments;
        this.submissions = submissions;
        this.feedback = feedback;
        this.contentProgress = contentProgress;
        this.moduleProgress = moduleProgress;
    }

    public boolean viewed(UUID contentId, UUID userId) {
        return contentProgress.findByUserIdAndContentId(userId, contentId).isPresent();
    }

    public boolean markedDone(UUID contentId, UUID userId) {
        return contentProgress.findByUserIdAndContentId(userId, contentId)
                .filter(p -> p.getStatus() == StudentContentProgressStatus.COMPLETED).isPresent();
    }

    public boolean moduleCompleted(UUID moduleId, UUID userId) {
        return moduleProgress.findByUserIdAndModuleIdIn(userId, List.of(moduleId)).stream()
                .anyMatch(p -> p.getProgressStatus() == ProgressStatus.COMPLETED);
    }

    public boolean handedIn(UUID assignmentId, UUID userId) {
        return submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, userId).stream()
                .anyMatch(s -> s.getSubmittedAt() != null && HANDED_IN.contains(s.getSubmissionStatus()));
    }

    /** Best posted (RETURNED) score as a percentage of the points possible; null when no grade is posted. */
    public BigDecimal postedPercent(UUID assignmentId, UUID userId) {
        Assignment a = assignments.findById(assignmentId).orElse(null);
        if (a == null || a.getPointsPossible() == null || a.getPointsPossible().signum() <= 0) return null;
        BigDecimal best = null;
        for (AssignmentSubmission s : submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, userId)) {
            if (s.getSubmissionStatus() != AssignmentSubmissionStatus.RETURNED) continue;
            Feedback f = feedback.findFirstBySubmissionIdOrderByDateGradedDesc(s.getSubmissionId()).orElse(null);
            if (f == null || f.getGradeEarned() == null) continue;
            if (best == null || f.getGradeEarned().compareTo(best) > 0) best = f.getGradeEarned();
        }
        return best == null ? null : best.multiply(BigDecimal.valueOf(100)).divide(a.getPointsPossible(), 2, RoundingMode.HALF_UP);
    }
}
