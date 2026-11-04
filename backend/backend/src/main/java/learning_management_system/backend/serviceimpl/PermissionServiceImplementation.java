package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.mapper.RoleMapper;
import learning_management_system.backend.repository.PermissionRepository;
import learning_management_system.backend.service.PermissionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionServiceImplementation implements PermissionService {
    private final PermissionRepository permissions;
    private final RoleMapper roleMapper;

    public PermissionServiceImplementation(PermissionRepository permissions, RoleMapper roleMapper) {
        this.permissions = permissions;
        this.roleMapper = roleMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PermissionDto> list() {
        return permissions.findAllByOrderByPermissionCode().stream().map(roleMapper::toDto).toList();
    }
}
