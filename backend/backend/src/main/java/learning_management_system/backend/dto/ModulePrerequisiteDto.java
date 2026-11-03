package learning_management_system.backend.dto;

import java.util.UUID;

/** A module that must be completed first (table module_prerequisite). */
public record ModulePrerequisiteDto(UUID modulePrerequisiteId, UUID moduleId, UUID prerequisiteModuleId, String prerequisiteModuleTitle) {}
