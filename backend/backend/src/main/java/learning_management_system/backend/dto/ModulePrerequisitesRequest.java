package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** Replace a module's prerequisites; each must be an earlier module of the same course. Empty list = none. */
public record ModulePrerequisitesRequest(@NotNull List<UUID> prerequisiteModuleIds) {}
