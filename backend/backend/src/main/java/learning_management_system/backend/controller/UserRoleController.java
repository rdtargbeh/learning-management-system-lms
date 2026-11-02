package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.UserRoleDto;
import learning_management_system.backend.service.UserRoleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** user_role: a user's role assignments. */
@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
public class UserRoleController {
    private final UserRoleService userRoles;

    public UserRoleController(UserRoleService userRoles) { this.userRoles = userRoles; }

    @GetMapping
    public List<UserRoleDto> list(@PathVariable UUID userId) { return userRoles.list(userId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRoleDto assign(@PathVariable UUID userId, @Valid @RequestBody AssignRoleRequest r) { return userRoles.assign(userId, r); }

    @DeleteMapping("/{userRoleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable UUID userId, @PathVariable UUID userRoleId) { userRoles.unassign(userId, userRoleId); }
}
