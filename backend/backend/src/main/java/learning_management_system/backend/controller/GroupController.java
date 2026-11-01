package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.CreateGroupSetRequest;
import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.GroupSetDto;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.service.GroupService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Group sets, groups and group members. */
@RestController
@RequestMapping("/api/v1")
public class GroupController {
    private final GroupService groups;

    public GroupController(GroupService groups) { this.groups = groups; }

    @GetMapping("/courses/{courseId}/group-sets")
    public List<GroupSetDto> sets(@PathVariable UUID courseId) { return groups.listSets(courseId); }

    @PostMapping("/courses/{courseId}/group-sets")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupSetDto createSet(@PathVariable UUID courseId, @Valid @RequestBody CreateGroupSetRequest r) {
        return groups.createSet(courseId, r);
    }

    @GetMapping("/group-sets/{groupSetId}/groups")
    public List<StudentGroupDto> groupsOf(@PathVariable UUID groupSetId) { return groups.listGroups(groupSetId); }

    @PostMapping("/group-sets/{groupSetId}/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentGroupDto createGroup(@PathVariable UUID groupSetId, @Valid @RequestBody CreateStudentGroupRequest r) {
        return groups.createGroup(groupSetId, r);
    }

    @GetMapping("/groups/{groupId}/members")
    public List<GroupMemberDto> members(@PathVariable UUID groupId) { return groups.members(groupId); }

    /** Teachers add anyone; in a self-signup set students send an empty body to join. */
    @PostMapping("/groups/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberDto addMember(@PathVariable UUID groupId, @RequestBody(required = false) AddGroupMemberRequest r) {
        return groups.addMember(groupId, r);
    }

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID groupId, @PathVariable UUID userId) { groups.removeMember(groupId, userId); }
}
