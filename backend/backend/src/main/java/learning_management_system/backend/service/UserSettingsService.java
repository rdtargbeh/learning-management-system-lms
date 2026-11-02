package learning_management_system.backend.service;

import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.dto.UserPreferencesDto;

import java.util.UUID;

/**
 * A user's accessibility settings and preferences. Users read and change their own (PATCH: null leaves a value unchanged); institution staff with USER_READ
 * can read a user's accessibility settings (e.g. to apply extended time).
 */
public interface UserSettingsService {

    AccessibilitySettingsDto accessibility(UUID userId);

    AccessibilitySettingsDto updateAccessibility(AccessibilitySettingsDto d);

    UserPreferencesDto preferences();

    UserPreferencesDto updatePreferences(UserPreferencesDto d);
}
