package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignRoleRequest;
import learning_management_system.backend.dto.CreateRoleRequest;
import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.dto.UpdateRoleRequest;
import learning_management_system.backend.dto.UserRoleDto;
import learning_management_system.backend.entity.Permission;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.entity.RolePermission;
import learning_management_system.backend.entity.UserRole;
import learning_management_system.backend.enums.RoleLevel;
import learning_management_system.backend.enums.RoleScope;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.PermissionRepository;
import learning_management_system.backend.repository.RolePermissionRepository;
import learning_management_system.backend.repository.RoleRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.repository.UserRoleRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.RoleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static learning_management_system.backend.utility.Patch.set;

/** Tenant roles, their permissions, and role assignments. Shared role templates (tenant_id NULL) are read-only here. */
@Service
public class RoleServiceImplementation implements RoleService {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionRepository rolePermissions;
    private final UserRoleRepository userRoles;
    private final UserRepository users;
    private final RoleMapper roleMapper;

    public RoleServiceImplementation(RoleRepository roles, PermissionRepository permissions, RolePermissionRepository rolePermissions,
                                     UserRoleRepository userRoles, UserRepository users, RoleMapper roleMapper) {
        this.roles = roles;
        this.permissions = permissions;
        this.rolePermissions = rolePermissions;
        this.userRoles = userRoles;
        this.users = users;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoleDto> listRoles() {
        Access.requireTenant();
        return roles.findByRoleLevelOrderByRoleName(RoleLevel.TENANT).stream().map(r -> roleMapper.toDto(r, codes(r.getRoleId()))).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PermissionDto> listPermissions() {
        return permissions.findAllByOrderByPermissionCode().stream().map(roleMapper::toDto).toList();
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
        replacePermissions(role, r.permissions());
        return roleMapper.toDto(role, codes(role.getRoleId()));
    }

    @Transactional
    @Override
    public RoleDto update(UUID roleId, UpdateRoleRequest r) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        Role role = editable(roleId);
        set(r.roleName(), role::setRoleName);
        set(r.description(), role::setDescription);
        set(r.isActive(), role::setIsActive);
        roles.save(role);
        if (r.permissions() != null) replacePermissions(role, r.permissions());
        return roleMapper.toDto(role, codes(roleId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserRoleDto> userRoles(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        Map<UUID, String> names = new HashMap<>();
        return userRoles.findByUserId(userId).stream().map(ur -> roleMapper.toDto(ur,
                names.computeIfAbsent(ur.getRoleId(), id -> roles.findById(id).map(Role::getRoleName).orElse(null)))).toList();
    }

    /** The database trigger rejects platform roles for tenant users and roles from another tenant. */
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
        return roleMapper.toDto(ur, role.getRoleName());
    }

    @Transactional
    @Override
    public void unassign(UUID userId, UUID userRoleId) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        UserRole ur = userRoles.findById(userRoleId).filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("Role assignment"));
        userRoles.delete(ur);
    }

    private Role editable(UUID roleId) {
        Role role = roles.findById(roleId).orElseThrow(() -> ApiException.notFound("Role"));
        if (role.getTenantId() == null) throw ApiException.forbidden("Shared role templates cannot be edited by an institution");
        return role;
    }

    private void replacePermissions(Role role, List<String> codes) {
        rolePermissions.deleteByRoleId(role.getRoleId());
        rolePermissions.flush();
        if (codes == null) return;
        Map<String, Permission> byCode = new HashMap<>();
        permissions.findAll().forEach(p -> byCode.put(p.getPermissionCode(), p));
        for (String code : new LinkedHashSet<>(codes)) {
            Permission p = byCode.get(code);
            if (p == null) throw ApiException.badRequest("UNKNOWN_PERMISSION", "Unknown permission " + code);
            RolePermission rp = new RolePermission();
            rp.setRoleId(role.getRoleId());
            rp.setPermissionId(p.getPermissionId());
            rolePermissions.save(rp);
        }
    }

    private List<String> codes(UUID roleId) {
        return rolePermissions.findPermissionCodesForRoles(List.of(roleId)).stream().sorted().toList();
    }
}
