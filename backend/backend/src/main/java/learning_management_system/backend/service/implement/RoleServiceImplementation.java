package learning_management_system.backend.service.implement;

import jakarta.transaction.Transactional;
import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.dto.RoleDto;
import learning_management_system.backend.entity.Permission;
import learning_management_system.backend.entity.Role;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.RoleType;
import learning_management_system.backend.mapper.PermissionMapper;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.PermissionRepository;
import learning_management_system.backend.repository.RoleRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.service.ActivityLogsService;
import learning_management_system.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImplementation implements RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private  PermissionRepository permissionRepository;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private ActivityLogsService activityLogsService;




    @Override
//    @Transactional
    public RoleDto createRole(RoleDto roleDto, Long createdById) {
        validateRoleUniqueness(roleDto, null);
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found with ID: " + createdById));

        Tenant tenant = null;
        if (roleDto.getTenantId() != null) {
            tenant = tenantRepository.findById(roleDto.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + roleDto.getTenantId()));
        }

        Role role = roleMapper.toEntity(roleDto, createdBy);
        role.setTenant(tenant);

        // ✅ Ensure `isActive` is always set before saving
        if (role.getActive() == null) {
            role.setActive(true);
        }

        // ✅ Ensure `isDeleted` is always set before saving
        if (role.getDeleted() == null) {
            role.setDeleted(false);
        }

        Role savedRole = roleRepository.save(role);
        return roleMapper.toDto(savedRole);
    }


    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
        // Fetch the existing role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Check for duplicate role name within the same tenant
        if (roleDto.getTenantId() != null) {
            roleRepository.findByRoleNameAndTenant_TenantId(roleDto.getRoleName(), roleDto.getTenantId())
                    .ifPresent(existingRole -> {
                        if (!existingRole.getRoleId().equals(roleId)) {
                            throw new RuntimeException("Role with the same name already exists for the tenant.");
                        }
                    });
        } else {
            roleRepository.findByRoleNameAndTenant_TenantId(roleDto.getRoleName(), null)
                    .ifPresent(existingRole -> {
                        if (!existingRole.getRoleId().equals(roleId)) {
                            throw new RuntimeException("Global role with the same name already exists.");
                        }
                    });
        }
        // Update fields of the existing role using RoleDto
        mapRoleDtoToEntity(roleDto, role); // Inline method to handle in-place updates

        // Set updatedBy user
        User updatedBy = userRepository.findById(roleDto.getUpdatedBy())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + roleDto.getUpdatedBy()));

        role.setUpdatedBy(updatedBy);

        role.setRoleName(roleDto.getRoleName());
        role.setDescription(roleDto.getDescription());
        role.setRoleType(roleDto.getRoleType());
        role.setPriority(role.getPriority());

        // Save the updated role
        Role updatedRole = roleRepository.save(role);

        // Convert the updated entity back to DTO and return
        return roleMapper.toDto(updatedRole);
    }


    private void mapRoleDtoToEntity(RoleDto roleDto, Role role) {
        if (roleDto == null || role == null) {
            throw new IllegalArgumentException("RoleDto or Role cannot be null");
        }

        role.setRoleName(roleDto.getRoleName());
        role.setDescription(roleDto.getDescription());

        if (roleDto.getRoleType() != null) {
            String roleType = roleDto.getRoleType().toString(); // Trim to remove any leading/trailing whitespace
            try {
                role.setRoleType(RoleType.valueOf(roleType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role type: " + roleDto.getRoleType());
            }
        } else {
            role.setRoleType(null); // Or handle it with a default value if needed
        }


        role.setPriority(roleDto.getPriority());
        role.setActive(roleDto.getActive());
        role.setDeleted(roleDto.getDeleted());
        role.setMetadata(roleDto.getMetadata());

        // Handle Tenant and ParentRole in the service layer as needed
    }

    @Override
    public List<Role> getAllRoles() {
        // Enhance to include sorting and filtering (e.g., by roleName)
        return roleRepository.findAll();
    }

    @Override
    public RoleDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        return roleMapper.toDto(role);
    }

    @Override
    public List<RoleDto> getRolesByTenant(Long tenantId) {
        return roleRepository.findByTenant_TenantId(tenantId).stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDto> getRolesByType(RoleType roleType) {
        return roleRepository.findByRoleType(roleType).stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRole(Long roleId) {
        // Validate before deletion
        if (!roleRepository.existsById(roleId)) {
            throw new IllegalArgumentException("Role not found with id " + roleId);
        }
        roleRepository.deleteById(roleId);
    }

    @Override
    public Set<User> getUsersByRoleId(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        return role.getUsers(); // Access users associated with this role
    }


    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // Fetch the role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found."));
        // Fetch the permissions
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissions.isEmpty()) {
            throw new RuntimeException("No valid permissions found for the given IDs.");
        }

        // Validate and assign permissions
        for (Permission permission : permissions) {
            if (!Boolean.TRUE.equals(permission.getActive())) {
                throw new RuntimeException("Inactive permission: " + permission.getPermissionName());
            }
            // Check cross-tenant assignment
            if (permission.getRole() != null &&
                    !Objects.equals(Optional.ofNullable(permission.getRole().getTenant()).orElse(null),
                            Optional.ofNullable(role.getTenant()).orElse(null))) {
                throw new RuntimeException("Cross-tenant permission assignment is not allowed.");
            }
            // Assign permission only if it's not already assigned
            if (!role.getPermissions().contains(permission)) {
                permission.setRole(role);
            }
        }

//        permissions.forEach(permission -> {
//            if (!permission.getActive()) {
//                throw new RuntimeException("Inactive permission: " + permission.getPermissionName());
//            }
//            // Validate cross-tenant assignment
//            if (permission.getRole() != null && !permission.getRole().getTenant().equals(role.getTenant())) {
//                throw new RuntimeException("Cross-tenant permission assignment is not allowed.");
//            }
//            // Assign permission to the role
//            permission.setRole(role);
//        });

        // Update role's permissions
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }


    @Override
    @Transactional
    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found."));

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        permissions.forEach(permission -> {
            if (permission.getRole() == null || !permission.getRole().getRoleId().equals(roleId)) {
                throw new RuntimeException("Permission not associated with this role: " + permission.getPermissionName());
            }
            permission.setRole(null);
        });

        role.getPermissions().removeAll(permissions);
        roleRepository.save(role);
    }

    @Override
    public Page<RoleDto> getRolesByCriteria(String roleType, Boolean isActive, Long tenantId, Pageable pageable) {
        return roleRepository.findByCriteria(roleType, isActive, tenantId, pageable)
                .map(roleMapper::toDto);
    }


    @Override
    public Page<RoleDto> getRoles(Pageable pageable) {
        return roleRepository.findAll((Pageable) pageable)
                .map(roleMapper::toDto);
    }


    @Override
    public Set<PermissionDto> getEffectivePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found."));

        Set<Permission> effectivePermissions = new HashSet<>(role.getPermissions());
        Role parentRole = role.getParentRole();

        while (parentRole != null) {
            effectivePermissions.addAll(parentRole.getPermissions());
            parentRole = parentRole.getParentRole();
        }

        // Optional: Cache effective permissions
        return effectivePermissions.stream()
                .filter(Permission::isActiveAndValid)
                .map(permissionMapper::toDto)
                .collect(Collectors.toSet());
    }

    private void validateRoleUniqueness(RoleDto roleDto) {
        validateRoleUniqueness(roleDto, null);
    }

    private void validateRoleUniqueness(RoleDto roleDto, Long existingRoleId) {
        roleRepository.findByRoleNameAndTenant_TenantIdAndIsDeletedFalse(
                        roleDto.getRoleName(), roleDto.getTenantId())
                .ifPresent(existingRole -> {
                    if (existingRoleId == null || !existingRole.getRoleId().equals(existingRoleId)) {
                        throw new RuntimeException("Role with the same name already exists.");
                    }
                });
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Current user not found!"));
    }




//    @Override
//    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
//        Role role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
//
//        // Check for duplicate role name within the same tenant
//        if (roleDto.getTenantId() != null) {
//            roleRepository.findByRoleNameAndTenant_TenantId(roleDto.getRoleName(), roleDto.getTenantId())
//                    .ifPresent(existingRole -> {
//                        if (!existingRole.getRoleId().equals(roleId)) {
//                            throw new RuntimeException("Role with the same name already exists for the tenant.");
//                        }
//                    });
//        } else {
//            roleRepository.findByRoleNameAndTenant_TenantId(roleDto.getRoleName(), null)
//                    .ifPresent(existingRole -> {
//                        if (!existingRole.getRoleId().equals(roleId)) {
//                            throw new RuntimeException("Global role with the same name already exists.");
//                        }
//                    });
//        }
//
//        // Update role fields
//        RoleMapper.toEntity(roleDto);
//
//        // Set updatedBy user
//        User updatedBy = userRepository.findById(roleDto.getUpdatedBy())
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + roleDto.getUpdatedBy()));
//
//        role.setUpdatedBy(updatedBy);
//
//        role.setRoleName(roleDto.getRoleName());
//        role.setDescription(roleDto.getDescription());
//        role.setRoleType(roleDto.getRoleType());
//        role.setPriority(role.getPriority());
//
//        return roleMapper.toDto(roleRepository.save(role));
//    }


}
