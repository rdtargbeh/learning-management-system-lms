package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentGradingType;
import learning_management_system.backend.enums.AssignmentType;

/**
 * Create an assignment (unpublished). assignmentType decides how students submit: TEXT_ENTRY, ESSAY and
 * CODE_SUBMISSION take text, URL_SUBMISSION takes a link, ON_PAPER and NO_SUBMISSION are graded without an online
 * submission. groupSetId makes it a group assignment.
 */
public record CreateAssignmentRequest(@NotBlank @Size(min = 3, max = 255) String assignmentTitle, String assignmentDescription,
                                      String assignmentInstructions, @NotNull AssignmentType assignmentType,
                                      @Positive BigDecimal pointsPossible, AssignmentGradingType gradingType, UUID moduleId,
                                      LocalDate dateAvailable, LocalDate dateDue, LocalDate dateClose, @Positive Integer maxSubmissions,
                                      Boolean lateSubmissionAllowed,
                                      @DecimalMin("0") @DecimalMax("100") BigDecimal lateSubmissionPenaltyPercent,
                                      Boolean omitFromFinalGrade, Boolean isAnonymousGrading, UUID groupSetId,
                                      Boolean gradeGroupIndividually, Boolean peerReviewRequired, @Positive Integer peerReviewCount) {}
