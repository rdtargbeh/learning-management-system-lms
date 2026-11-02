package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.PlatformUserProfile;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.PlatformRole;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.UserMapper;
import learning_management_system.backend.repository.PlatformUserProfileRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.PlatformUserProfileService;
import learning_management_system.backend.service.UserProvisioningService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** platform_user_profile: platform staff (super admins, platform admins, support, ...). */
@Service
public class PlatformUserProfileServiceImplementation implements PlatformUserProfileService {
    private final UserRepository users;
    private final PlatformUserProfileRepository platformProfiles;
    private final UserProvisioningService provisioning;
    private final UserService userService;
    private final UserMapper userMapper;
    private final SystemTx sys;
    private final AuditLogService audit;

    public PlatformUserProfileServiceImplementation(UserRepository users, PlatformUserProfileRepository platformProfiles,
                                                    UserProvisioningService provisioning, UserService userService,
                                                    UserMapper userMapper, SystemTx sys, AuditLogService audit) {
        this.users = users;
        this.platformProfiles = platformProfiles;
        this.provisioning = provisioning;
        this.userService = userService;
        this.userMapper = userMapper;
        this.sys = sys;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public PlatformUserProfileDto get(UUID userId) {
        Access.requirePlatformUser();
        if (!Access.ctx().platformAdmin() && !Access.isSelf(userId)) throw ApiException.forbidden("Platform admins only");
        return sys.platform(() -> platformProfiles.findByUserId(userId).map(userMapper::toDto)
                .orElseThrow(() -> ApiException.notFound("Platform user")));
    }

    @Transactional
    @Override
    public UserDetailDto create(CreatePlatformUserRequest r) {
        Access.requirePlatformAdmin();
        boolean callerIsSuper = sys.platform(() -> platformProfiles.findByUserId(Access.ctx().userId())
                .map(p -> p.getPlatformRole() == PlatformRole.SUPER_ADMIN).orElse(false));
        if (r.platformRole() == PlatformRole.SUPER_ADMIN && !callerIsSuper) {
            throw ApiException.forbidden("Only a super admin can create another super admin");
        }
        User u = provisioning.provisionPlatformUser(r, Access.ctx().userId());
        return userService.toDetail(u);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryDto> list() {
        Access.requirePlatformAdmin();
        return userService.toSummaries(users.findByIsSystemUserTrueOrderByUsername());
    }

    @Transactional
    @Override
    public PlatformUserProfileDto update(UUID userId, PlatformUserProfileDto d) {
        Access.requirePlatformAdmin();
        PlatformUserProfile p = platformProfiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Platform user"));
        if (d.platformRole() == PlatformRole.SUPER_ADMIN) {
            boolean callerIsSuper = platformProfiles.findByUserId(Access.ctx().userId())
                    .map(x -> x.getPlatformRole() == PlatformRole.SUPER_ADMIN).orElse(false);
            if (!callerIsSuper) throw ApiException.forbidden("Only a super admin can grant super admin");
        }
        userMapper.updateEntity(d, p);
        PlatformUserProfileDto out = userMapper.toDto(platformProfiles.save(p));
        audit.record("PLATFORM_PROFILE_UPDATED", "PLATFORM_USER_PROFILE", p.getPlatformProfileId(), userId, Severity.WARNING, null, d);
        return out;
    }
}
