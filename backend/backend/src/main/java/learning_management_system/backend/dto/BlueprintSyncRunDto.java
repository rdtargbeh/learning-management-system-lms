package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.enums.BlueprintSyncRunStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One sync of a blueprint (table blueprint_sync_run). changes: counts per course and the blueprint items pushed;
 * exceptions: items an associated course changed or deleted, which the sync left alone.
 */
public record BlueprintSyncRunDto(UUID syncRunId, UUID blueprintCourseId, UUID initiatedBy, String syncComment, Boolean includeCourseSettings,
                                  Boolean notifyUsers, BlueprintSyncRunStatus runStatus, JsonNode changes, Integer coursesSynced,
                                  JsonNode exceptions, OffsetDateTime dateStart, OffsetDateTime dateEnd, OffsetDateTime dateCreated) {}
