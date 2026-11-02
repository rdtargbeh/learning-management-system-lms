package learning_management_system.backend.service;

import learning_management_system.backend.dto.UserPreferencesDto;

import java.util.UUID;

/** user_preferences: theme, language, notification and dashboard preferences. */
public interface UserPreferencesService {

    /** The caller's preferences (userId null), or another user's with USER_READ. Created with defaults on first use. */
    UserPreferencesDto get(UUID userId);

    /** PATCH of the caller's own preferences. */
    UserPreferencesDto update(UserPreferencesDto d);
}
