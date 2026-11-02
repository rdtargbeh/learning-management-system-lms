package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AcademicAdvisorProfileDto;
import learning_management_system.backend.entity.AcademicAdvisorProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.AcademicAdvisorProfileRepository;
import learning_management_system.backend.repository.EmployeeProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AcademicAdvisorProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** academic_advisor_profile; an advisor must have an employee profile first. */
@Service
public class AcademicAdvisorProfileServiceImplementation implements AcademicAdvisorProfileService {
    private final AcademicAdvisorProfileRepository advisors;
    private final EmployeeProfileRepository employees;
    private final UserRepository users;
    private final UserMapper userMapper;

    public AcademicAdvisorProfileServiceImplementation(AcademicAdvisorProfileRepository advisors, EmployeeProfileRepository employees,
                                                       UserRepository users, UserMapper userMapper) {
        this.advisors = advisors;
        this.employees = employees;
        this.users = users;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public AcademicAdvisorProfileDto get(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        tenantUser(userId);
        return advisors.findByUserId(userId).map(userMapper::toDto).orElseThrow(() -> ApiException.notFound("Advisor profile"));
    }

    @Transactional
    @Override
    public AcademicAdvisorProfileDto update(UUID userId, AcademicAdvisorProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = tenantUser(userId);
        AcademicAdvisorProfile a = advisors.findByUserId(userId).orElseGet(() -> {
            if (employees.findByUserId(userId).isEmpty()) {
                throw ApiException.badRequest("EMPLOYEE_PROFILE_REQUIRED", "An advisor must have an employee profile first");
            }
            AcademicAdvisorProfile n = new AcademicAdvisorProfile();
            n.setUserId(userId);
            n.setTenantId(u.getTenantId());
            return n;
        });
        userMapper.updateEntity(d, a);
        return userMapper.toDto(advisors.save(a));
    }

    private User tenantUser(UUID userId) {
        return users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
    }
}
