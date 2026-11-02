package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.entity.UserProfile;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.UserProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.UserProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** user_profile; the database trigger creates it with the account. */
@Service
public class UserProfileServiceImplementation implements UserProfileService {
    private final UserProfileRepository profiles;
    private final UserRepository users;
    private final UserMapper userMapper;

    public UserProfileServiceImplementation(UserProfileRepository profiles, UserRepository users, UserMapper userMapper) {
        this.profiles = profiles;
        this.users = users;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public UserProfileDto get(UUID userId) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_READ);
        return userMapper.toDto(load(userId));
    }

    /** Everyone edits their own profile; USER_MANAGE edits anyone's in the institution. */
    @Transactional
    @Override
    public UserProfileDto update(UUID userId, UserProfileDto d) {
        if (!Access.isSelf(userId)) Access.requireTenantPermission(Permissions.USER_MANAGE);
        UserProfile p = load(userId);
        userMapper.updateEntity(d, p);
        return userMapper.toDto(profiles.save(p));
    }

    private UserProfile load(UUID userId) {
        // platform staff profiles are only reachable by the platform user themself
        users.findById(userId).filter(u -> !Boolean.TRUE.equals(u.getIsSystemUser()) || Access.isSelf(userId))
                .orElseThrow(() -> ApiException.notFound("User"));
        return profiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Profile"));
    }
}
