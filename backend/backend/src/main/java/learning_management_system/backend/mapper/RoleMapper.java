package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UserRoleDto;
import learning_management_system.backend.entity.Permission;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** roles, permissions and user_role. */
@Mapper(config = MapStructConfig.class)
public interface RoleMapper {

    @Mapping(target = "shared", expression = "java(role.getTenantId() == null)")
    @Mapping(target = "permissions", source = "permissionCodes")
    RoleDto toDto(Role role, List<String> permissionCodes);

    PermissionDto toDto(Permission permission);

    @Mapping(target = "roleName", source = "roleName")
    UserRoleDto toDto(UserRole userRole, String roleName);
}
