package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import learning_management_system.backend.enums.AssignmentSubmissionStatus;
import learning_management_system.backend.enums.LatePolicyStatus;

/**
 * The caller's own situation on an assignment: their dates (after overrides), attempts, status and posted grade.
 * canSubmit says whether a new submission is accepted now; lockReason says why not.
 */
public record StudentAssignmentStatusDto(OffsetDateTime dateAvailable, OffsetDateTime dateDue, OffsetDateTime dateClose,
                                         boolean canSubmit, String lockReason, int attemptsUsed, Integer attemptsAllowed,
                                         AssignmentSubmissionStatus submissionStatus, LatePolicyStatus latePolicyStatus,
                                         Boolean excused, BigDecimal score, String gradeLetter) {}
