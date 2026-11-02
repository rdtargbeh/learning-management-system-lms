package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;
import learning_management_system.backend.service.RoleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** roles of the caller's institution. */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roles;

    public RoleController(RoleService roles) { this.roles = roles; }

    @GetMapping
    public List<RoleDto> list() { return roles.list(); }

    @GetMapping("/{roleId}")
    public RoleDto get(@PathVariable UUID roleId) { return roles.get(roleId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto create(@Valid @RequestBody CreateRoleRequest r) { return roles.create(r); }

    @PatchMapping("/{roleId}")
    public RoleDto update(@PathVariable UUID roleId, @Valid @RequestBody UpdateRoleRequest r) { return roles.update(roleId, r); }
}
