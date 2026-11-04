package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssignmentDto;
import learning_management_system.backend.dto.AssignmentOverrideDto;
import learning_management_system.backend.dto.AssignmentViewDto;
import learning_management_system.backend.dto.CreateAssignmentOverrideRequest;
import learning_management_system.backend.dto.CreateAssignmentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.UpdateAssignmentRequest;
import learning_management_system.backend.service.AssignmentService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Assignments and their date overrides. */
@RestController
@RequestMapping("/api/v1")
public class AssignmentController {
    private final AssignmentService assignments;

    public AssignmentController(AssignmentService assignments) { this.assignments = assignments; }

    @GetMapping("/courses/{courseId}/assignments")
    public List<AssignmentViewDto> list(@PathVariable UUID courseId) { return assignments.list(courseId); }

    @PostMapping("/courses/{courseId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateAssignmentRequest r) {
        return assignments.create(courseId, r);
    }

    @GetMapping("/assignments/{assignmentId}")
    public AssignmentViewDto get(@PathVariable UUID assignmentId) { return assignments.get(assignmentId); }

    @PatchMapping("/assignments/{assignmentId}")
    public AssignmentDto update(@PathVariable UUID assignmentId, @Valid @RequestBody UpdateAssignmentRequest r) {
        return assignments.update(assignmentId, r);
    }

    @PostMapping("/assignments/{assignmentId}/publish")
    public AssignmentDto publish(@PathVariable UUID assignmentId) { return assignments.setPublished(assignmentId, true); }

    @PostMapping("/assignments/{assignmentId}/unpublish")
    public AssignmentDto unpublish(@PathVariable UUID assignmentId) { return assignments.setPublished(assignmentId, false); }

    @PostMapping("/assignments/{assignmentId}/move")
    public AssignmentDto move(@PathVariable UUID assignmentId, @RequestBody MoveContentRequest r) { return assignments.move(assignmentId, r); }

    @DeleteMapping("/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID assignmentId) { assignments.delete(assignmentId); }

    @GetMapping("/assignments/{assignmentId}/overrides")
    public List<AssignmentOverrideDto> overrides(@PathVariable UUID assignmentId) { return assignments.overrides(assignmentId); }

    @PostMapping("/assignments/{assignmentId}/overrides")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentOverrideDto addOverride(@PathVariable UUID assignmentId, @Valid @RequestBody CreateAssignmentOverrideRequest r) {
        return assignments.addOverride(assignmentId, r);
    }

    @DeleteMapping("/assignments/{assignmentId}/overrides/{overrideId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeOverride(@PathVariable UUID assignmentId, @PathVariable UUID overrideId) {
        assignments.removeOverride(assignmentId, overrideId);
    }
}
