package learning_management_system.backend.controller;

import learning_management_system.backend.dto.UserProfileDto;
import learning_management_system.backend.service.UserProfileService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** user_profile: name, contact details, locale and other personal fields every user has. Users edit their own. */
@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
public class UserProfileController {
    private final UserProfileService profiles;

    public UserProfileController(UserProfileService profiles) { this.profiles = profiles; }

    @GetMapping
    public UserProfileDto get(@PathVariable UUID userId) { return profiles.get(userId); }

    @PatchMapping
    public UserProfileDto update(@PathVariable UUID userId, @RequestBody UserProfileDto d) { return profiles.update(userId, d); }
}
