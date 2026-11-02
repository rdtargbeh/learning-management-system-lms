package learning_management_system.backend.controller;

import learning_management_system.backend.dto.BackupCodesDto;
import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.DisableMfaRequest;
import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.MfaCodeRequest;
import learning_management_system.backend.dto.MfaStatusDto;
import learning_management_system.backend.dto.MfaVerifyRequest;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.RefreshRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;
import learning_management_system.backend.dto.SessionDto;
import learning_management_system.backend.dto.TotpSetupDto;
import learning_management_system.backend.dto.UserDetailDto;
import learning_management_system.backend.dto.VerifyEmailRequest;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.service.AccountRecoveryService;
import learning_management_system.backend.service.AuthService;
import learning_management_system.backend.service.MfaService;
import learning_management_system.backend.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/** Sign-in, sessions, MFA, password recovery and email verification. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    private final SessionService sessions;
    private final MfaService mfa;
    private final AccountRecoveryService recovery;

    public AuthController(AuthService auth, SessionService sessions, MfaService mfa, AccountRecoveryService recovery) {
        this.auth = auth;
        this.sessions = sessions;
        this.mfa = mfa;
        this.recovery = recovery;
    }

    // ---------------------------------------------------------------- sign-in (no token needed)

    /** Platform staff: omit tenantCode. Institution users: send the institution's tenantCode. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r, HttpServletRequest http) {
        return auth.login(r, clientIp(http), http.getHeader("User-Agent"));
    }

    /** Second step when login answered mfaRequired = true. */
    @PostMapping("/mfa/verify")
    public LoginResponse verifyMfa(@Valid @RequestBody MfaVerifyRequest r, HttpServletRequest http) {
        return auth.verifyMfa(r, clientIp(http), http.getHeader("User-Agent"));
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshRequest r, HttpServletRequest http) {
        return sessions.refresh(r.refreshToken(), clientIp(http), http.getHeader("User-Agent"));
    }

    /** Always 202, whether or not the account exists. */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest r, HttpServletRequest http) {
        recovery.forgotPassword(r, clientIp(http), http.getHeader("User-Agent"));
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest r) { recovery.resetPassword(r); }

    @PostMapping("/email/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest r) { recovery.verifyEmail(r.token()); }

    // ---------------------------------------------------------------- the signed-in user

    @GetMapping("/me")
    public UserDetailDto me() { return auth.me(); }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() { sessions.logout(); }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r) { auth.changeOwnPassword(r); }

    @PostMapping("/email/verify/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestEmailVerification() { recovery.requestEmailVerification(); }

    @GetMapping("/sessions")
    public List<SessionDto> sessions() { return sessions.mine(); }

    /** Sign out one device (own sessions; others' with USER_MANAGE). */
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID sessionId) { sessions.revoke(sessionId); }

    @GetMapping("/login-history")
    public PageResponse<LoginHistoryDto> loginHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return auth.loginHistory(Access.ctx().userId(), page, size);
    }

    // ---------------------------------------------------------------- MFA

    @GetMapping("/mfa")
    public MfaStatusDto mfaStatus() { return mfa.status(); }

    @PostMapping("/mfa/totp/setup")
    public TotpSetupDto setupTotp() { return mfa.setupTotp(); }

    @PostMapping("/mfa/totp/enable")
    public BackupCodesDto enableTotp(@Valid @RequestBody MfaCodeRequest r) { return mfa.enableTotp(r.code()); }

    @PostMapping("/mfa/backup-codes")
    public BackupCodesDto backupCodes(@Valid @RequestBody MfaCodeRequest r) { return mfa.regenerateBackupCodes(r.code()); }

    @PostMapping("/mfa/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableMfa(@Valid @RequestBody DisableMfaRequest r) { mfa.disable(r); }

    private static InetAddress clientIp(HttpServletRequest http) {
        try {
            return InetAddress.getByName(http.getRemoteAddr());
        } catch (Exception e) {
            return null;
        }
    }
}
