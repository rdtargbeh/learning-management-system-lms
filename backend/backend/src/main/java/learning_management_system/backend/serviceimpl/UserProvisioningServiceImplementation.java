package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.CreateUserRequest;
import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.entity.UserProfile;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.AuthProvider;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.UserProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.RequestContext;
import learning_management_system.backend.service.TenantPlanService;
import learning_management_system.backend.service.UserProvisioningService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * The single code path that creates accounts in {@code users}. Callers must already be authorized and inside a
 * transaction. The database trigger creates user_profile and the profile for the user_type.
 */
@Service
public class UserProvisioningServiceImplementation implements UserProvisioningService {
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final PlatformUserProfileRepository platformProfiles;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final TenantPlanService plans;

    public UserProvisioningServiceImplementation(UserRepository users, UserProfileRepository profiles,
                                                 PlatformUserProfileRepository platformProfiles, PasswordEncoder encoder,
                                                 UserMapper userMapper, TenantPlanService plans) {
        this.users = users;
        this.profiles = profiles;
        this.platformProfiles = platformProfiles;
        this.encoder = encoder;
        this.userMapper = userMapper;
        this.plans = plans;
    }

    @Override
    public User provisionTenantUser(CreateUserRequest r, UUID tenantId) {
        plans.requireRoom(tenantId, r.userType());
        User u = newTenantUser(r, tenantId, RequestContext.current().userId());
        users.saveAndFlush(u);
        plans.refreshCounts(tenantId);
        applyProfile(u.getUserId(), r.profile());
        return u;
    }

    @Override
    public User provisionPlatformUser(CreatePlatformUserRequest r, UUID createdBy) {
        User u = new User();
        u.setTenantId(null);
        u.setIsSystemUser(true);
        u.setUserType(UserType.PLATFORM);
        u.setUsername(r.username().trim());
        u.setEmail(r.email().trim());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setPasswordLastChanged(OffsetDateTime.now());
        u.setAccountStatus(AccountStatus.ACTIVE);
        u.setAuthProvider(AuthProvider.LOCAL);
        u.setMfaRequired(true);                      // required for every platform user by the schema
        u.setCreatedBy(createdBy);
        users.saveAndFlush(u);
        PlatformUserProfile p = platformProfiles.findByUserId(u.getUserId()).orElseThrow();
        p.setPlatformRole(r.platformRole());
        p.setJobTitle(r.jobTitle());
        p.setDepartment(r.department());
        p.setCanImpersonate(r.canImpersonate() != null && r.canImpersonate());
        platformProfiles.save(p);
        applyProfile(u.getUserId(), r.profile());
        return u;
    }

    private User newTenantUser(CreateUserRequest r, UUID tenantId, UUID createdBy) {
        User u = new User();
        u.setTenantId(tenantId);
        u.setIsSystemUser(false);
        u.setUserType(r.userType());
        u.setUsername(r.username().trim());
        u.setEmail(r.email().trim());
        u.setAuthProvider(r.authProvider() == null ? AuthProvider.LOCAL : r.authProvider());
        u.setSisSourceId(r.sisSourceId());
        u.setCreatedBy(createdBy);
        if (r.password() != null) {
            u.setPasswordHash(encoder.encode(r.password()));
            u.setPasswordLastChanged(OffsetDateTime.now());
            u.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            u.setAccountStatus(u.getAuthProvider() == AuthProvider.LOCAL ? AccountStatus.INVITED : AccountStatus.ACTIVE);
        }
        return u;
    }

    private void applyProfile(UUID userId, UserProfileDto d) {
        if (d == null) return;
        UserProfile p = profiles.findByUserId(userId).orElseThrow();
        userMapper.updateEntity(d, p);
        profiles.save(p);
    }
}
