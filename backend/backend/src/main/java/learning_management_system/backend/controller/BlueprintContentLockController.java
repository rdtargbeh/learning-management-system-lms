package learning_management_system.backend.controller;

import learning_management_system.backend.dto.BlueprintContentLockDto;
import learning_management_system.backend.dto.SetBlueprintLockRequest;
import learning_management_system.backend.service.BlueprintContentLockService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** blueprint_content_lock: the blueprint's locked items. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/blueprint/locks")
public class BlueprintContentLockController {
    private final BlueprintContentLockService locks;

    public BlueprintContentLockController(BlueprintContentLockService locks) { this.locks = locks; }

    @GetMapping
    public List<BlueprintContentLockDto> list(@PathVariable UUID courseId) { return locks.list(courseId); }

    /** Creates or replaces the item's lock; 204 when every flag was off (lock removed). */
    @PutMapping
    public ResponseEntity<BlueprintContentLockDto> set(@PathVariable UUID courseId, @Valid @RequestBody SetBlueprintLockRequest r) {
        BlueprintContentLockDto d = locks.set(courseId, r);
        return d == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(d);
    }

    @DeleteMapping("/{lockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID courseId, @PathVariable UUID lockId) { locks.remove(courseId, lockId); }
}
