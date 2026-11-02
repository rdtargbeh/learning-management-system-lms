package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AccessibilitySettingsDto;
import learning_management_system.backend.dto.AccommodationDto;
import learning_management_system.backend.dto.AccountLockDto;
import learning_management_system.backend.dto.CreateAccommodationRequest;
import learning_management_system.backend.dto.CreateUserLoginRequest;
import learning_management_system.backend.dto.LockAccountRequest;
import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.UserLoginDto;
import learning_management_system.backend.service.AccommodationService;
import learning_management_system.backend.service.AccountLockService;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.MfaService;
import learning_management_system.backend.service.UserLoginService;
import learning_management_system.backend.service.UserSettingsService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Administration of one user's account: locks, MFA reset, extra logins, sign-in history, accessibility, accommodations. */
@RestController
@RequestMapping("/api/v1/users/{userId}")
public class UserAccountController {
    private final AccountLockService locks;
    private final MfaService mfa;
    private final UserLoginService logins;
    private final AuthService auth;
    private final UserSettingsService settings;
    private final AccommodationService accommodations;

    public UserAccountController(AccountLockService locks, MfaService mfa, UserLoginService logins, AuthService auth,
                                 UserSettingsService settings, AccommodationService accommodations) {
        this.locks = locks;
        this.mfa = mfa;
        this.logins = logins;
        this.auth = auth;
        this.settings = settings;
        this.accommodations = accommodations;
    }

    @GetMapping("/lock")
    public AccountLockDto lock(@PathVariable UUID userId) { return locks.get(userId); }

    @PostMapping("/lock")
    public AccountLockDto lock(@PathVariable UUID userId, @Valid @RequestBody LockAccountRequest r) { return locks.lock(userId, r); }

    @PostMapping("/unlock")
    public AccountLockDto unlock(@PathVariable UUID userId) { return locks.unlock(userId); }

    @PostMapping("/mfa/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetMfa(@PathVariable UUID userId) { mfa.adminReset(userId); }

    @GetMapping("/logins")
    public List<UserLoginDto> logins(@PathVariable UUID userId) { return logins.list(userId); }

    @PostMapping("/logins")
    @ResponseStatus(HttpStatus.CREATED)
    public UserLoginDto addLogin(@PathVariable UUID userId, @Valid @RequestBody CreateUserLoginRequest r) { return logins.add(userId, r); }

    @DeleteMapping("/logins/{loginId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLogin(@PathVariable UUID userId, @PathVariable UUID loginId) { logins.remove(userId, loginId); }

    @GetMapping("/login-history")
    public PageResponse<LoginHistoryDto> loginHistory(@PathVariable UUID userId, @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "50") int size) {
        return auth.loginHistory(userId, page, size);
    }

    @GetMapping("/accessibility")
    public AccessibilitySettingsDto accessibility(@PathVariable UUID userId) { return settings.accessibility(userId); }

    @GetMapping("/accommodations")
    public List<AccommodationDto> accommodations(@PathVariable UUID userId) { return accommodations.forUser(userId); }

    @PostMapping("/accommodations")
    @ResponseStatus(HttpStatus.CREATED)
    public AccommodationDto addAccommodation(@PathVariable UUID userId, @Valid @RequestBody CreateAccommodationRequest r) {
        return accommodations.create(userId, r);
    }
}
