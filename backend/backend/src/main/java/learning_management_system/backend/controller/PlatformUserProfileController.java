package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CreatePlatformUserRequest;
import learning_management_system.backend.dto.PlatformUserProfileDto;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.service.PlatformUserProfileService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** platform_user_profile: platform staff (platform users only). */
@RestController
@RequestMapping("/api/v1/platform/users")
public class PlatformUserProfileController {
    private final PlatformUserProfileService platformUsers;

    public PlatformUserProfileController(PlatformUserProfileService platformUsers) { this.platformUsers = platformUsers; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailDto create(@Valid @RequestBody CreatePlatformUserRequest r) { return platformUsers.create(r); }

    @GetMapping
    public List<UserSummaryDto> list() { return platformUsers.list(); }

    @GetMapping("/{userId}/platform-profile")
    public PlatformUserProfileDto get(@PathVariable UUID userId) { return platformUsers.get(userId); }

    @PatchMapping("/{userId}/platform-profile")
    public PlatformUserProfileDto update(@PathVariable UUID userId, @RequestBody PlatformUserProfileDto d) { return platformUsers.update(userId, d); }
}
