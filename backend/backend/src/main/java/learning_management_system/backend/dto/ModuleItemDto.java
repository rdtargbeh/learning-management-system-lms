package learning_management_system.backend.dto;

import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;

/** An item in a module as one person sees it. completed and locked are filled for students. */
public record ModuleItemDto(UUID contentId, String contentTitle, String typeCode, Boolean isPublished, Integer displayOrder,
                            ModuleCompletionRequirementType requirementType, Boolean completed, Boolean locked) {}
