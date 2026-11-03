package learning_management_system.backend.controller;

import learning_management_system.backend.dto.BlueprintSyncRunDto;
import learning_management_system.backend.dto.StartBlueprintSyncRequest;
import learning_management_system.backend.service.BlueprintSyncRunService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** blueprint_sync_run: start a sync and read the sync history. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/blueprint/syncs")
public class BlueprintSyncRunController {
    private final BlueprintSyncRunService syncs;

    public BlueprintSyncRunController(BlueprintSyncRunService syncs) { this.syncs = syncs; }

    @GetMapping
    public List<BlueprintSyncRunDto> list(@PathVariable UUID courseId) { return syncs.list(courseId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlueprintSyncRunDto sync(@PathVariable UUID courseId, @Valid @RequestBody(required = false) StartBlueprintSyncRequest r) {
        return syncs.sync(courseId, r);
    }

    @GetMapping("/{syncRunId}")
    public BlueprintSyncRunDto get(@PathVariable UUID courseId, @PathVariable UUID syncRunId) { return syncs.get(courseId, syncRunId); }
}
