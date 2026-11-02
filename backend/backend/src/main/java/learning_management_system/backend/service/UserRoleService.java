package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.UserRoleDto;

import java.util.List;
import java.util.UUID;

/** user_role: which roles a user holds (tenant-wide or scoped). Permissions are read at sign-in. */
public interface UserRoleService {

    /** Own roles, or anyone's with USER_READ. */
    List<UserRoleDto> list(UUID userId);

    /** ROLE_MANAGE. The database trigger rejects platform roles for tenant users and roles from another tenant. */
    UserRoleDto assign(UUID userId, AssignRoleRequest r);

    void unassign(UUID userId, UUID userRoleId);

    /** Permission codes the user holds through roles (no permission check: used at sign-in). */
    List<String> permissionsOf(UUID userId);
}
