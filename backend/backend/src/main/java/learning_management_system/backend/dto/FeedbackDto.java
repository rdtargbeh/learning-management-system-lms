package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A grade and comment from one grader (table feedback). */
public record FeedbackDto(UUID feedbackId, UUID evaluatorId, BigDecimal gradeEarned, BigDecimal gradePercentage, String gradeLetter,
                          String feedbackText, Boolean isPassed, OffsetDateTime dateGraded) {}
