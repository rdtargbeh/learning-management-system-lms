package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.MyGroupDto;
import learning_management_system.backend.service.GroupMemberService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** group_members: a group's members, and the caller's own groups in a course. */
@RestController
@RequestMapping("/api/v1")
public class GroupMemberController {
    private final GroupMemberService members;

    public GroupMemberController(GroupMemberService members) { this.members = members; }

    @GetMapping("/groups/{groupId}/members")
    public List<GroupMemberDto> list(@PathVariable UUID groupId) { return members.list(groupId); }

    /** Body optional: without userId the caller joins (self sign-up). */
    @PostMapping("/groups/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberDto add(@PathVariable UUID groupId, @Valid @RequestBody(required = false) AddGroupMemberRequest r) {
        return members.add(groupId, r);
    }

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID groupId, @PathVariable UUID userId) { members.remove(groupId, userId); }

    @GetMapping("/courses/{courseId}/groups/mine")
    public List<MyGroupDto> mine(@PathVariable UUID courseId) { return members.mine(courseId); }
}
