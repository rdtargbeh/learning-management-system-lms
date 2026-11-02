package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.enums.RoleLevel;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.RoleRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.RolePermissionService;
import learning_management_system.backend.service.RoleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static learning_management_system.backend.utility.Patch.set;

@Service
public class RoleServiceImplementation implements RoleService {
    private final RoleRepository roles;
    private final RolePermissionService rolePermissions;
    private final RoleMapper roleMapper;
    private final AuditLogService audit;

    public RoleServiceImplementation(RoleRepository roles, RolePermissionService rolePermissions, RoleMapper roleMapper,
                                     AuditLogService audit) {
        this.roles = roles;
        this.rolePermissions = rolePermissions;
        this.roleMapper = roleMapper;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoleDto> list() {
        Access.requireTenant();
        return roles.findByRoleLevelOrderByRoleName(RoleLevel.TENANT).stream().map(r -> roleMapper.toDto(r, rolePermissions.codes(r.getRoleId()))).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public RoleDto get(UUID roleId) {
        Access.requireTenant();
        Role role = roles.findById(roleId).orElseThrow(() -> ApiException.notFound("Role"));
        return roleMapper.toDto(role, rolePermissions.codes(roleId));
    }

    @Transactional
    @Override
    public RoleDto create(CreateRoleRequest r) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        Role role = new Role();
        role.setRoleName(r.roleName().trim());
        role.setRoleCode(r.roleCode() == null ? r.roleName().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_") : r.roleCode());
        role.setRoleLevel(RoleLevel.TENANT);
        role.setDescription(r.description());
        role.setIsSystemRole(false);
        roles.saveAndFlush(role);
        if (r.permissions() != null) rolePermissions.replace(role.getRoleId(), r.permissions());
        audit.record("ROLE_CREATED", "ROLE", role.getRoleId(), null, Severity.INFO, null, Map.of("roleName", role.getRoleName()));
        return roleMapper.toDto(role, rolePermissions.codes(role.getRoleId()));
    }

    @Transactional
    @Override
    public RoleDto update(UUID roleId, UpdateRoleRequest r) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        Role role = roles.findById(roleId).orElseThrow(() -> ApiException.notFound("Role"));
        if (role.getTenantId() == null) throw ApiException.forbidden("Shared role templates cannot be edited by an institution");
        set(r.roleName(), role::setRoleName);
        set(r.description(), role::setDescription);
        set(r.isActive(), role::setIsActive);
        roles.save(role);
        if (r.permissions() != null) rolePermissions.replace(roleId, r.permissions());
        return roleMapper.toDto(role, rolePermissions.codes(roleId));
    }
}
