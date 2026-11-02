package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.UserRoleDto;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.entity.UserRole;
import learning_management_system.backend.enums.RoleScope;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.RoleRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.UserRoleRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.RolePermissionService;
import learning_management_system.backend.service.UserRoleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserRoleServiceImplementation implements UserRoleService {
    private final UserRoleRepository userRoles;
    private final RoleRepository roles;
    private final UserRepository users;
    private final RolePermissionService rolePermissions;
    private final RoleMapper roleMapper;
    private final AuditLogService audit;

    public UserRoleServiceImplementation(UserRoleRepository userRoles, RoleRepository roles, UserRepository users,
                                         RolePermissionService rolePermissions, RoleMapper roleMapper, AuditLogService audit) {
        this.userRoles = userRoles;
        this.roles = roles;
        this.users = users;
        this.rolePermissions = rolePermissions;
        this.roleMapper = roleMapper;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserRoleDto> list(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        Map<UUID, String> names = new HashMap<>();
        return userRoles.findByUserId(userId).stream().map(ur -> roleMapper.toDto(ur,
                names.computeIfAbsent(ur.getRoleId(), id -> roles.findById(id).map(Role::getRoleName).orElse(null)))).toList();
    }

    @Transactional
    @Override
    public UserRoleDto assign(UUID userId, AssignRoleRequest r) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
        Role role = roles.findById(r.roleId()).orElseThrow(() -> ApiException.notFound("Role"));
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getRoleId());
        ur.setRoleScope(r.roleScope() == null ? RoleScope.TENANT : r.roleScope());
        ur.setScopeEntityId(r.scopeEntityId());
        ur.setIsPrimaryRole(r.isPrimaryRole() != null && r.isPrimaryRole());
        ur.setDateAssigned(LocalDate.now());
        ur.setDateAssignedBy(Access.ctx().userId());
        userRoles.saveAndFlush(ur);
        audit.record("ROLE_ASSIGNED", "USER_ROLE", ur.getUserRoleId(), userId, Severity.INFO, null, Map.of("role", role.getRoleName()));
        return roleMapper.toDto(ur, role.getRoleName());
    }

    @Transactional
    @Override
    public void unassign(UUID userId, UUID userRoleId) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        UserRole ur = userRoles.findById(userRoleId).filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("Role assignment"));
        userRoles.delete(ur);
        audit.record("ROLE_UNASSIGNED", "USER_ROLE", userRoleId, userId, Severity.INFO, null, null);
    }

    @Override
    public List<String> permissionsOf(UUID userId) {
        return rolePermissions.codesForRoles(userRoles.findByUserId(userId).stream().map(UserRole::getRoleId).toList());
    }
}
