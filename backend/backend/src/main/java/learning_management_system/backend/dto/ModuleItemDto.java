package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;

/** An item in a module (content or assignment) as one person sees it. completed and locked are filled for students. */
public record ModuleItemDto(String itemType, UUID itemId, UUID contentId, UUID assignmentId, String title, String typeCode,
                            Boolean isPublished, Integer displayOrder, ModuleCompletionRequirementType requirementType,
                            BigDecimal minScore, Boolean completed, Boolean locked) {}
