package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;

/**
 * Require an item of the module: VIEW (open it) or MARK_DONE (press "Mark as done"). Assignment, quiz and
 * discussion requirements come with those features.
 */
public record AddModuleRequirementRequest(@NotNull UUID contentId, @NotNull ModuleCompletionRequirementType requirementType) {}
