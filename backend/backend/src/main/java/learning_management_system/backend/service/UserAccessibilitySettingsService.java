package learning_management_system.backend.service;

import learning_management_system.backend.dto.AccessibilitySettingsDto;

import java.util.UUID;

/** user_accessibility_settings: text size, contrast, captions, screen reader, extended time ... */
public interface UserAccessibilitySettingsService {

    /** The caller's settings (userId null), or another user's with USER_READ. Created with defaults on first use. */
    AccessibilitySettingsDto get(UUID userId);

    /** PATCH of the caller's own settings. */
    AccessibilitySettingsDto update(AccessibilitySettingsDto d);
}
