package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import learning_management_system.backend.enums.AssignmentGradingType;
import learning_management_system.backend.enums.AssignmentType;

/** PATCH an assignment; null fields are left unchanged. */
public record UpdateAssignmentRequest(@Size(min = 3, max = 255) String assignmentTitle, String assignmentDescription,
                                      String assignmentInstructions, AssignmentType assignmentType, @Positive BigDecimal pointsPossible,
                                      AssignmentGradingType gradingType, LocalDate dateAvailable, LocalDate dateDue,
                                      LocalDate dateClose, @Positive Integer maxSubmissions, Boolean lateSubmissionAllowed,
                                      @DecimalMin("0") @DecimalMax("100") BigDecimal lateSubmissionPenaltyPercent,
                                      Boolean omitFromFinalGrade, Boolean isAnonymousGrading, Boolean gradeGroupIndividually,
                                      Boolean peerReviewRequired, @Positive Integer peerReviewCount) {}
