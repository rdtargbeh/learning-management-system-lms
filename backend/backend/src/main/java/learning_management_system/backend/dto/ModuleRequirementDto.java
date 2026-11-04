package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;

/** What a student must do with an item to complete a module (table module_completion_requirement). */
public record ModuleRequirementDto(UUID requirementId, UUID contentId, UUID assignmentId, UUID assessmentId, UUID discussionId,
                                   ModuleCompletionRequirementType requirementType, BigDecimal minScore, Integer displayOrder) {}
