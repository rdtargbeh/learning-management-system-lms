package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.CalculationMethod;
import learning_management_system.backend.enums.LearningOutcomeBloomLevel;
import learning_management_system.backend.enums.OutcomeType;

import java.math.BigDecimal;

/**
 * Creates an outcome (outcomeDescription required) or changes one (PATCH: null leaves a field unchanged).
 * calculationInt: DECAYING_AVERAGE weight of the latest result, 1-99 (default 65); N_MASTERY number of times, 1-10
 * (default 5); other methods take none. outcomeNumber defaults to the next free number.
 */
public record LearningOutcomeRequest(@Positive Integer outcomeNumber, @Size(min = 10, max = 5000) String outcomeDescription,
                                     LearningOutcomeBloomLevel bloomLevel, @Positive BigDecimal masteryPoints,
                                     CalculationMethod calculationMethod, Integer calculationInt, OutcomeType outcomeType,
                                     Boolean isPrimary, @Size(max = 100) String assessmentMethod, Boolean isActive) {}
