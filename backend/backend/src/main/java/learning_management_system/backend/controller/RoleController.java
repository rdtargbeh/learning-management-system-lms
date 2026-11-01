package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreateObserverLinkRequest;
import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;
import learning_management_system.backend.service.ObserverService;
import learning_management_system.backend.service.RoleService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roles;
    private final ObserverService observers;

    public RoleController(RoleService roles, ObserverService observers) {
        this.roles = roles;
        this.observers = observers;
    }

    @GetMapping("/roles")
    public List<RoleDto> listRoles() { return roles.listRoles(); }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleDto createRole(@Valid @RequestBody CreateRoleRequest r) { return roles.create(r); }

    @PatchMapping("/roles/{roleId}")
    public RoleDto updateRole(@PathVariable UUID roleId, @Valid @RequestBody UpdateRoleRequest r) { return roles.update(roleId, r); }

    @GetMapping("/permissions")
    public List<PermissionDto> permissions() { return roles.listPermissions(); }

    @PostMapping("/observer-links")
    @ResponseStatus(HttpStatus.CREATED)
    public ObserverStudentLinkDto link(@Valid @RequestBody CreateObserverLinkRequest r) { return observers.link(r); }

    @PostMapping("/observer-links/{linkId}/revoke")
    public ObserverStudentLinkDto revoke(@PathVariable UUID linkId) { return observers.revoke(linkId); }
}
