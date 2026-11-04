package learning_management_system.backend.service;

import learning_management_system.backend.dto.BlueprintSyncRunDto;
import learning_management_system.backend.dto.StartBlueprintSyncRequest;

import java.util.List;
import java.util.UUID;

/**
 * blueprint_sync_run: pushing a blueprint course's modules, pages and assignments (and optionally its settings) to the
 * associated courses. The blueprint's teachers (or COURSE_MANAGE) start syncs and read their history.
 */
public interface BlueprintSyncRunService {

    List<BlueprintSyncRunDto> list(UUID blueprintCourseId);

    BlueprintSyncRunDto get(UUID blueprintCourseId, UUID syncRunId);

    /** Runs the sync now and returns the finished run (409 SYNC_RUNNING while another one runs). */
    BlueprintSyncRunDto sync(UUID blueprintCourseId, StartBlueprintSyncRequest r);
}
