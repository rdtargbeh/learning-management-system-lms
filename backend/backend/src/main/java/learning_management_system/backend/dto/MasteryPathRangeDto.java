package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** A score range of a mastery path (table mastery_path_range): lower bound included, upper excluded (the top range includes its upper bound). */
public record MasteryPathRangeDto(UUID rangeId, UUID ruleId, BigDecimal lowerBoundPercent, BigDecimal upperBoundPercent, Integer displayOrder,
                                  List<MasteryPathItemDto> items) {}
