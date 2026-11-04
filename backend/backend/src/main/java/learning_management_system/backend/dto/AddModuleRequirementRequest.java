package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;

/**
 * Require an item of the module. For a content item: VIEW or MARK_DONE. For an assignment: SUBMIT, or MIN_SCORE
 * with minScore in points (counts once grades are posted). Give contentId or assignmentId.
 */
public record AddModuleRequirementRequest(UUID contentId, UUID assignmentId, @NotNull ModuleCompletionRequirementType requirementType,
                                          @DecimalMin("0") BigDecimal minScore) {}
