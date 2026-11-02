package learning_management_system.backend.service;

import learning_management_system.backend.dto.PermissionDto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** role_permissions: which permissions a role grants. Shared role templates (tenant_id NULL) cannot be changed here. */
public interface RolePermissionService {

    List<PermissionDto> list(UUID roleId);

    /** Replaces the role's permissions (ROLE_MANAGE). Unknown codes give 400 UNKNOWN_PERMISSION. */
    List<PermissionDto> replace(UUID roleId, List<String> permissionCodes);

    /** Sorted permission codes of one role (no permission check: for building views). */
    List<String> codes(UUID roleId);

    /** Distinct, sorted active permission codes granted by any of these roles. */
    List<String> codesForRoles(Collection<UUID> roleIds);
}
