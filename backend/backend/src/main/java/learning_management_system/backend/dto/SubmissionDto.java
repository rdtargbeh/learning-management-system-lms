package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.LatePolicyStatus;

/**
 * One attempt (table assignment_submission). Students see feedback only after grades are posted. With anonymous
 * grading, graders see no names.
 */
public record SubmissionDto(UUID submissionId, UUID assignmentId, UUID userId, String username, String displayName,
                            Integer submissionNumber, AssignmentSubmissionStatus submissionStatus, OffsetDateTime submittedAt,
                            Boolean submittedLate, LatePolicyStatus latePolicyStatus, Boolean isExcused, Boolean isGroupSubmission,
                            UUID groupId, String submissionText, String submissionUrl, String submissionNotes, Integer wordCount,
                            FeedbackDto feedback, OffsetDateTime dateUpdated) {}
