package learning_management_system.backend.controller;

import learning_management_system.backend.dto.*;
import learning_management_system.backend.service.GroupSetService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** group_set: a course's group sets. */
@RestController
@RequestMapping("/api/v1")
public class GroupSetController {
    private final GroupSetService groupSets;

    public GroupSetController(GroupSetService groupSets) { this.groupSets = groupSets; }

    @GetMapping("/courses/{courseId}/group-sets")
    public List<GroupSetDto> list(@PathVariable UUID courseId) { return groupSets.list(courseId); }

    @PostMapping("/courses/{courseId}/group-sets")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupSetDto create(@PathVariable UUID courseId, @Valid @RequestBody CreateGroupSetRequest r) { return groupSets.create(courseId, r); }

    @GetMapping("/group-sets/{groupSetId}")
    public GroupSetDto get(@PathVariable UUID groupSetId) { return groupSets.get(groupSetId); }

    @PatchMapping("/group-sets/{groupSetId}")
    public GroupSetDto update(@PathVariable UUID groupSetId, @Valid @RequestBody UpdateGroupSetRequest r) { return groupSets.update(groupSetId, r); }

    @DeleteMapping("/group-sets/{groupSetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID groupSetId) { groupSets.delete(groupSetId); }

    @PostMapping("/group-sets/{groupSetId}/auto-assign")
    public AutoAssignGroupsResultDto autoAssign(@PathVariable UUID groupSetId, @Valid @RequestBody(required = false) AutoAssignGroupsRequest r) {
        return groupSets.autoAssign(groupSetId, r);
    }
}
