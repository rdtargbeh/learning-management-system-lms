package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Grade an attempt. gradeEarned is in points (may exceed pointsPossible for extra credit). */
public record GradeSubmissionRequest(@DecimalMin("0") BigDecimal gradeEarned, @Size(max = 5) String gradeLetter, String feedbackText,
                                     Boolean isPassed) {}
