package learning_management_system.backend.dto;

import learning_management_system.backend.enums.BlueprintContentLockObjectType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * What associated courses may not change in a blueprint item (table blueprint_content_lock). objectId is the
 * blueprint's item (for SETTINGS: the blueprint course). copyId is set when the view is for an associated course.
 */
public record BlueprintContentLockDto(UUID lockId, UUID blueprintCourseId, BlueprintContentLockObjectType objectType, UUID objectId,
                                      UUID copyId, Boolean lockContent, Boolean lockPoints, Boolean lockDueDates,
                                      Boolean lockAvailabilityDates, Boolean lockSettings, OffsetDateTime dateCreated) {}
