package learning_management_system.backend.controller;

import learning_management_system.backend.dto.UserPreferencesDto;
import learning_management_system.backend.service.UserPreferencesService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** user_preferences: own preferences under /me, another user's (USER_READ) under /users/{id}. */
@RestController
@RequestMapping("/api/v1")
public class UserPreferencesController {
    private final UserPreferencesService preferences;

    public UserPreferencesController(UserPreferencesService preferences) { this.preferences = preferences; }

    @GetMapping("/me/preferences")
    public UserPreferencesDto mine() { return preferences.get(null); }

    @PatchMapping("/me/preferences")
    public UserPreferencesDto update(@Valid @RequestBody UserPreferencesDto d) { return preferences.update(d); }

    @GetMapping("/users/{userId}/preferences")
    public UserPreferencesDto get(@PathVariable UUID userId) { return preferences.get(userId); }
}
