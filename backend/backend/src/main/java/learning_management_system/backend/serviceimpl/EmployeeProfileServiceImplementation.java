package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.EmployeeProfileDto;
import learning_management_system.backend.entity.EmployeeProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.EmployeeProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.EmployeeProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** employee_profile; created by trigger for employee user types, or here (employeeType required). */
@Service
public class EmployeeProfileServiceImplementation implements EmployeeProfileService {
    private final EmployeeProfileRepository employees;
    private final UserRepository users;
    private final UserMapper userMapper;

    public EmployeeProfileServiceImplementation(EmployeeProfileRepository employees, UserRepository users, UserMapper userMapper) {
        this.employees = employees;
        this.users = users;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public EmployeeProfileDto get(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        tenantUser(userId);
        return employees.findByUserId(userId).map(userMapper::toDto).orElseThrow(() -> ApiException.notFound("Employee profile"));
    }

    @Transactional
    @Override
    public EmployeeProfileDto update(UUID userId, EmployeeProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = tenantUser(userId);
        EmployeeProfile e = employees.findByUserId(userId).orElseGet(() -> {
            if (d == null || d.employeeType() == null) {
                throw ApiException.badRequest("EMPLOYEE_TYPE_REQUIRED", "employeeType is required to add an employee profile");
            }
            EmployeeProfile n = new EmployeeProfile();
            n.setUserId(userId);
            n.setTenantId(u.getTenantId());
            return n;
        });
        userMapper.updateEntity(d, e);
        return userMapper.toDto(employees.save(e));
    }

    private User tenantUser(UUID userId) {
        return users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
    }
}
