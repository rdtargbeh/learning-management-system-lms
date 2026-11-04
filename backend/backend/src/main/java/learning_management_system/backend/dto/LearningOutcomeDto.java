package learning_management_system.backend.dto;

import learning_management_system.backend.enums.CalculationMethod;
import learning_management_system.backend.enums.LearningOutcomeBloomLevel;
import learning_management_system.backend.enums.OutcomeType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A learning outcome (table learning_outcome) owned by a course or by an org unit (program, department ...).
 * {@code owner} is COURSE or ORG_UNIT, so a course's list can show inherited program outcomes beside its own.
 */
public record LearningOutcomeDto(UUID outcomeId, UUID courseId, UUID orgUnitId, String owner, Integer outcomeNumber, String outcomeDescription,
                                 LearningOutcomeBloomLevel bloomLevel, BigDecimal masteryPoints, CalculationMethod calculationMethod,
                                 Integer calculationInt, OutcomeType outcomeType, Boolean isPrimary, String assessmentMethod,
                                 Boolean isActive, OffsetDateTime dateCreated, OffsetDateTime dateUpdated) {}
