package learning_management_system.backend.dto;

import java.util.UUID;

/** One step of a pace (table course_pace_item): an item of a module and how many (working) days it gets. */
public record CoursePaceItemDto(UUID paceItemId, UUID paceId, UUID moduleId, String moduleTitle, String itemType, UUID itemId, String title,
                                Integer durationDays, Integer displayOrder) {}
