package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;

import java.util.List;
import java.util.UUID;

/** platform_user_profile: platform staff accounts and their platform role, scope and rights. Platform admins manage them. */
public interface PlatformUserProfileService {

    /** Creates the account (users) with its platform profile. Only a super admin creates another super admin. */
    UserDetailDto create(CreatePlatformUserRequest r);

    List<UserSummaryDto> list();

    /** Platform admins, or the platform user themself. */
    PlatformUserProfileDto get(UUID userId);

    /** PATCH. Only a super admin grants SUPER_ADMIN. */
    PlatformUserProfileDto update(UUID userId, PlatformUserProfileDto d);
}
