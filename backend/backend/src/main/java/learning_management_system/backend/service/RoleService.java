package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;
import learning_management_system.backend.dto.UserRoleDto;

import java.util.List;
import java.util.UUID;

/**
 * Tenant roles, their permissions, and role assignments. Shared role templates (tenant_id NULL) are read-only
 * here.
 */
public interface RoleService {

    List<RoleDto> listRoles();

    List<PermissionDto> listPermissions();

    RoleDto create(CreateRoleRequest r);

    RoleDto update(UUID roleId, UpdateRoleRequest r);

    List<UserRoleDto> userRoles(UUID userId);

    /** The database trigger rejects platform roles for tenant users and roles from another tenant. */
    UserRoleDto assign(UUID userId, AssignRoleRequest r);

    void unassign(UUID userId, UUID userRoleId);
}
