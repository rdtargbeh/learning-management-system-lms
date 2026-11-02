package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.dto.UserPreferencesDto;
import learning_management_system.backend.service.UserSettingsService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** The signed-in user's own settings. */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final UserSettingsService settings;

    public MeController(UserSettingsService settings) { this.settings = settings; }

    @GetMapping("/accessibility")
    public AccessibilitySettingsDto accessibility() { return settings.accessibility(null); }

    @PatchMapping("/accessibility")
    public AccessibilitySettingsDto updateAccessibility(@Valid @RequestBody AccessibilitySettingsDto d) { return settings.updateAccessibility(d); }

    @GetMapping("/preferences")
    public UserPreferencesDto preferences() { return settings.preferences(); }

    @PatchMapping("/preferences")
    public UserPreferencesDto updatePreferences(@Valid @RequestBody UserPreferencesDto d) { return settings.updatePreferences(d); }
}
