package learning_management_system.backend.service;

import learning_management_system.backend.dto.UserProfileDto;

import java.util.UUID;

/** user_profile: name, contact details, locale and other personal fields every user has. Users edit their own. */
public interface UserProfileService {

    /** The user themself, or anyone with USER_READ. */
    UserProfileDto get(UUID userId);

    /** PATCH: null values leave a field unchanged. */
    UserProfileDto update(UUID userId, UserProfileDto d);
}
