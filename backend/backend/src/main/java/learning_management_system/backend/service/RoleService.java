package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;

import java.util.List;
import java.util.UUID;

/** roles: the institution's roles. Shared role templates (tenant_id NULL) are read-only here. */
public interface RoleService {

    List<RoleDto> list();

    RoleDto get(UUID roleId);

    /** Creates a role, optionally with its permissions (stored through role_permissions). */
    RoleDto create(CreateRoleRequest r);

    /** PATCH; a permissions list replaces the role's permissions. */
    RoleDto update(UUID roleId, UpdateRoleRequest r);
}
