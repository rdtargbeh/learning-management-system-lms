package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.dto.UpdateStudentGroupRequest;
import learning_management_system.backend.service.StudentGroupService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** student_group: the groups of a set. */
@RestController
@RequestMapping("/api/v1")
public class StudentGroupController {
    private final StudentGroupService groups;

    public StudentGroupController(StudentGroupService groups) { this.groups = groups; }

    @GetMapping("/group-sets/{groupSetId}/groups")
    public List<StudentGroupDto> list(@PathVariable UUID groupSetId) { return groups.list(groupSetId); }

    @PostMapping("/group-sets/{groupSetId}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentGroupDto create(@PathVariable UUID groupSetId, @Valid @RequestBody CreateStudentGroupRequest r) { return groups.create(groupSetId, r); }

    @GetMapping("/groups/{groupId}")
    public StudentGroupDto get(@PathVariable UUID groupId) { return groups.get(groupId); }

    @PatchMapping("/groups/{groupId}")
    public StudentGroupDto update(@PathVariable UUID groupId, @Valid @RequestBody UpdateStudentGroupRequest r) { return groups.update(groupId, r); }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID groupId) { groups.delete(groupId); }
}
