package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.MfaVerifyRequest;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.utility.ClientInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Sign-in and the signed-in user's own account. Sessions, MFA, password reset and email verification have their own
 * controllers under /auth as well.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    /** Platform staff: omit tenantCode. Institution users: send the institution's tenantCode. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r, HttpServletRequest http) {
        return auth.login(r, ClientInfo.ip(http), ClientInfo.userAgent(http));
    }

    /** Second step when login answered mfaRequired = true. */
    @PostMapping("/mfa/verify")
    public LoginResponse verifyMfa(@Valid @RequestBody MfaVerifyRequest r, HttpServletRequest http) {
        return auth.verifyMfa(r, ClientInfo.ip(http), ClientInfo.userAgent(http));
    }

    @GetMapping("/me")
    public UserDetailDto me() { return auth.me(); }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r) { auth.changeOwnPassword(r); }
}
