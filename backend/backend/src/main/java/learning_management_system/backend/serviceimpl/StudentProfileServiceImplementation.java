package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.StudentProfileDto;
import learning_management_system.backend.entity.StudentProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.StudentProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.StudentProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** student_profile; created by trigger for students, or here when another user type needs one. */
@Service
public class StudentProfileServiceImplementation implements StudentProfileService {
    private final StudentProfileRepository students;
    private final UserRepository users;
    private final UserMapper userMapper;

    public StudentProfileServiceImplementation(StudentProfileRepository students, UserRepository users, UserMapper userMapper) {
        this.students = students;
        this.users = users;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public StudentProfileDto get(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        tenantUser(userId);
        return students.findByUserId(userId).map(userMapper::toDto).orElseThrow(() -> ApiException.notFound("Student profile"));
    }

    @Transactional
    @Override
    public StudentProfileDto update(UUID userId, StudentProfileDto d) {
        Access.requireTenantPermission(Permissions.USER_MANAGE);
        User u = tenantUser(userId);
        StudentProfile s = students.findByUserId(userId).orElseGet(() -> {
            StudentProfile n = new StudentProfile();
            n.setUserId(userId);
            n.setTenantId(u.getTenantId());
            return n;
        });
        userMapper.updateEntity(d, s);
        return userMapper.toDto(students.save(s));
    }

    private User tenantUser(UUID userId) {
        return users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser())).orElseThrow(() -> ApiException.notFound("User"));
    }
}
