package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LatePolicyStatus;

/** A student's row in the grader's list: their latest attempt and whether the work is missing. */
public record StudentSubmissionSummaryDto(UUID userId, String username, String displayName, int attempts, SubmissionDto latest,
                                          LatePolicyStatus latePolicyStatus, boolean missing, OffsetDateTime dateDue) {}
