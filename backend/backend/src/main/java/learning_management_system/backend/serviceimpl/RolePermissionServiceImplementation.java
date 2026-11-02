package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.entity.Permission;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.entity.RolePermission;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.PermissionRepository;
import learning_management_system.backend.repository.RolePermissionRepository;
import learning_management_system.backend.repository.RoleRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.RolePermissionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RolePermissionServiceImplementation implements RolePermissionService {
    private final RolePermissionRepository rolePermissions;
    private final PermissionRepository permissions;
    private final RoleRepository roles;
    private final RoleMapper roleMapper;
    private final AuditLogService audit;

    public RolePermissionServiceImplementation(RolePermissionRepository rolePermissions, PermissionRepository permissions,
                                               RoleRepository roles, RoleMapper roleMapper, AuditLogService audit) {
        this.rolePermissions = rolePermissions;
        this.permissions = permissions;
        this.roles = roles;
        this.roleMapper = roleMapper;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PermissionDto> list(UUID roleId) {
        Access.requireTenant();
        roles.findById(roleId).orElseThrow(() -> ApiException.notFound("Role"));
        return view(roleId);
    }

    @Transactional
    @Override
    public List<PermissionDto> replace(UUID roleId, List<String> permissionCodes) {
        Access.requireTenantPermission(Permissions.ROLE_MANAGE);
        Role role = roles.findById(roleId).orElseThrow(() -> ApiException.notFound("Role"));
        if (role.getTenantId() == null) throw ApiException.forbidden("Shared role templates cannot be edited by an institution");
        Map<String, Permission> byCode = new HashMap<>();
        permissions.findAll().forEach(p -> byCode.put(p.getPermissionCode(), p));
        Set<String> wanted = new LinkedHashSet<>(permissionCodes == null ? List.of() : permissionCodes);
        for (String code : wanted) {
            if (!byCode.containsKey(code)) throw ApiException.badRequest("UNKNOWN_PERMISSION", "Unknown permission " + code);
        }
        List<String> before = codes(roleId);
        rolePermissions.deleteByRoleId(roleId);
        rolePermissions.flush();
        for (String code : wanted) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(byCode.get(code).getPermissionId());
            rolePermissions.save(rp);
        }
        rolePermissions.flush();
        if (!before.equals(codes(roleId))) {
            audit.record("ROLE_PERMISSIONS_CHANGED", "ROLE", roleId, null, Severity.WARNING, Map.of("permissions", before),
                    Map.of("permissions", codes(roleId)));
        }
        return view(roleId);
    }

    @Override
    public List<String> codes(UUID roleId) {
        return rolePermissions.findPermissionCodesForRoles(List.of(roleId)).stream().sorted().toList();
    }

    @Override
    public List<String> codesForRoles(Collection<UUID> roleIds) {
        if (roleIds.isEmpty()) return List.of();
        return rolePermissions.findPermissionCodesForRoles(new ArrayList<>(roleIds)).stream().distinct().sorted().toList();
    }

    private List<PermissionDto> view(UUID roleId) {
        Set<UUID> ids = new HashSet<>();
        rolePermissions.findByRoleId(roleId).forEach(rp -> ids.add(rp.getPermissionId()));
        return permissions.findAllByOrderByPermissionCode().stream().filter(p -> ids.contains(p.getPermissionId()))
                .map(roleMapper::toDto).toList();
    }
}
