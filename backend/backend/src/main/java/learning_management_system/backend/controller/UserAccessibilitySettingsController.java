package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.service.UserAccessibilitySettingsService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** user_accessibility_settings: own settings under /me, another user's (USER_READ) under /users/{id}. */
@RestController
@RequestMapping("/api/v1")
public class UserAccessibilitySettingsController {
    private final UserAccessibilitySettingsService accessibility;

    public UserAccessibilitySettingsController(UserAccessibilitySettingsService accessibility) { this.accessibility = accessibility; }

    @GetMapping("/me/accessibility")
    public AccessibilitySettingsDto mine() { return accessibility.get(null); }

    @PatchMapping("/me/accessibility")
    public AccessibilitySettingsDto update(@Valid @RequestBody AccessibilitySettingsDto d) { return accessibility.update(d); }

    @GetMapping("/users/{userId}/accessibility")
    public AccessibilitySettingsDto get(@PathVariable UUID userId) { return accessibility.get(userId); }
}
