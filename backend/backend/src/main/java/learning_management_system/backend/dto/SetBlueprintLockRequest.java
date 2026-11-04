package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;

import java.util.UUID;

/**
 * Locks a blueprint item (CONTENT, ASSIGNMENT or MODULE; SETTINGS locks the course settings and needs no objectId).
 * lockContent defaults to true; turning every flag off removes the lock.
 */
public record SetBlueprintLockRequest(@NotNull BlueprintContentLockObjectType objectType, UUID objectId, Boolean lockContent,
                                      Boolean lockPoints, Boolean lockDueDates, Boolean lockAvailabilityDates, Boolean lockSettings) {}
