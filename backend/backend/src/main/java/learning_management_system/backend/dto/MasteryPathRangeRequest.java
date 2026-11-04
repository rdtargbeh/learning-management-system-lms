package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/** Adds a range (both bounds required) or moves one (PATCH). Ranges of a path may touch but not overlap. */
public record MasteryPathRangeRequest(@DecimalMin("0") @DecimalMax("1000") BigDecimal lowerBoundPercent,
                                      @DecimalMin("0") @DecimalMax("1000") BigDecimal upperBoundPercent, Integer displayOrder) {}
