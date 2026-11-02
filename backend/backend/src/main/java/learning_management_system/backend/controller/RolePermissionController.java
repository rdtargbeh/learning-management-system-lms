package learning_management_system.backend.controller;

import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.ReplaceRolePermissionsRequest;
import learning_management_system.backend.service.RolePermissionService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** role_permissions: the permissions a role grants. */
@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
public class RolePermissionController {
    private final RolePermissionService rolePermissions;

    public RolePermissionController(RolePermissionService rolePermissions) { this.rolePermissions = rolePermissions; }

    @GetMapping
    public List<PermissionDto> list(@PathVariable UUID roleId) { return rolePermissions.list(roleId); }

    @PutMapping
    public List<PermissionDto> replace(@PathVariable UUID roleId, @Valid @RequestBody ReplaceRolePermissionsRequest r) {
        return rolePermissions.replace(roleId, r.permissions());
    }
}
