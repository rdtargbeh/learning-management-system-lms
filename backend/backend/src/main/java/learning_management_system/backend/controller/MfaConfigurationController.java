package learning_management_system.backend.controller;

import learning_management_system.backend.dto.BackupCodesDto;
import learning_management_system.backend.dto.DisableMfaRequest;
import learning_management_system.backend.dto.MfaCodeRequest;
import learning_management_system.backend.dto.MfaStatusDto;
import learning_management_system.backend.dto.TotpSetupDto;
import learning_management_system.backend.service.MfaConfigurationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** mfa_configuration: the caller's two-step sign-in (/auth/mfa) and the administrator reset. */
@RestController
@RequestMapping("/api/v1")
public class MfaConfigurationController {
    private final MfaConfigurationService mfa;

    public MfaConfigurationController(MfaConfigurationService mfa) { this.mfa = mfa; }

    @GetMapping("/auth/mfa")
    public MfaStatusDto status() { return mfa.status(); }

    @PostMapping("/auth/mfa/totp/setup")
    public TotpSetupDto setupTotp() { return mfa.setupTotp(); }

    @PostMapping("/auth/mfa/totp/enable")
    public BackupCodesDto enableTotp(@Valid @RequestBody MfaCodeRequest r) { return mfa.enableTotp(r.code()); }

    @PostMapping("/auth/mfa/backup-codes")
    public BackupCodesDto backupCodes(@Valid @RequestBody MfaCodeRequest r) { return mfa.regenerateBackupCodes(r.code()); }

    @PostMapping("/auth/mfa/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@Valid @RequestBody DisableMfaRequest r) { mfa.disable(r); }

    /** Lost phone: an administrator removes the user's MFA so they can set it up again. */
    @PostMapping("/users/{userId}/mfa/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable UUID userId) { mfa.adminReset(userId); }
}
