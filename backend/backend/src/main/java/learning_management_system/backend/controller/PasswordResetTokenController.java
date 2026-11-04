package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;
import learning_management_system.backend.service.PasswordResetTokenService;
import learning_management_system.backend.utility.ClientInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** password_reset_token: "forgot password" (no sign-in needed). */
@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordResetTokenController {
    private final PasswordResetTokenService resets;

    public PasswordResetTokenController(PasswordResetTokenService resets) { this.resets = resets; }

    /** Always 202, whether or not the account exists. */
    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgot(@Valid @RequestBody ForgotPasswordRequest r, HttpServletRequest http) {
        resets.forgot(r, ClientInfo.ip(http), ClientInfo.userAgent(http));
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody ResetPasswordRequest r) { resets.reset(r); }
}
