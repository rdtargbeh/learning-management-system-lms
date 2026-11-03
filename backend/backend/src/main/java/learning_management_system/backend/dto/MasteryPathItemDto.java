package learning_management_system.backend.dto;

import java.util.UUID;

/** One item a score range assigns (table mastery_path_set_item); itemType is CONTENT or ASSIGNMENT. */
public record MasteryPathItemDto(UUID setItemId, UUID rangeId, Integer setNumber, String itemType, UUID itemId, String title, Integer displayOrder) {}
