package learning_management_system.backend.service;

import learning_management_system.backend.enums.TargetType;

import java.util.UUID;

/**
 * Helper (no table of its own): combines release conditions (on the item and on its module) and mastery paths into one
 * answer: why a student cannot open an item now, or null. Staff are never asked.
 */
public interface ItemAvailabilityService {

    String lockReason(UUID courseId, TargetType type, UUID itemId, UUID moduleId, UUID userId);
}
