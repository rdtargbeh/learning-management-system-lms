package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** A student's place on a mastery path: their posted score, the range it falls in, and the items released to them. */
public record MyMasteryPathDto(UUID ruleId, UUID triggerAssignmentId, String triggerTitle, BigDecimal scorePercent, UUID rangeId,
                               List<MasteryPathItemDto> releasedItems) {}
