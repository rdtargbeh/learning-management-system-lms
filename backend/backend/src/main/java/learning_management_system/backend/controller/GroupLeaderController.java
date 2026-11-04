package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AppointGroupLeaderRequest;
import learning_management_system.backend.dto.GroupLeaderDto;
import learning_management_system.backend.service.GroupLeaderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** group_leader: a group's leaders. */
@RestController
@RequestMapping("/api/v1/groups/{groupId}/leaders")
public class GroupLeaderController {
    private final GroupLeaderService leaders;

    public GroupLeaderController(GroupLeaderService leaders) { this.leaders = leaders; }

    @GetMapping
    public List<GroupLeaderDto> list(@PathVariable UUID groupId) { return leaders.list(groupId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupLeaderDto appoint(@PathVariable UUID groupId, @Valid @RequestBody AppointGroupLeaderRequest r) { return leaders.appoint(groupId, r); }

    @PostMapping("/{leaderId}/end")
    public GroupLeaderDto end(@PathVariable UUID groupId, @PathVariable UUID leaderId) { return leaders.end(groupId, leaderId); }
}
