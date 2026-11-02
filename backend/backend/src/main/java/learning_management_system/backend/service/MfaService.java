package learning_management_system.backend.service;

import learning_management_system.backend.dto.BackupCodesDto;
import learning_management_system.backend.dto.DisableMfaRequest;
import learning_management_system.backend.dto.MfaStatusDto;
import learning_management_system.backend.dto.TotpSetupDto;

import java.util.UUID;

/**
 * Multi-factor sign-in with an authenticator app (TOTP) and one-time backup codes (table mfa_configuration). Setup is
 * two steps: get a secret / QR link, then confirm with a code. Accounts flagged mfa_required (every platform user) cannot
 * turn MFA off. Five wrong codes in a row lock the account for 15 minutes.
 */
public interface MfaService {

    MfaStatusDto status();

    TotpSetupDto setupTotp();

    /** Confirms the setup with a code and returns 10 backup codes (shown once). */
    BackupCodesDto enableTotp(String code);

    BackupCodesDto regenerateBackupCodes(String code);

    void disable(DisableMfaRequest r);

    /** An administrator removes a user's MFA (lost phone); the user sets it up again. */
    void adminReset(UUID userId);

    /** For sign-in (trusted context): is MFA on for this user? */
    boolean isEnabled(UUID userId);

    /** For sign-in (trusted context): checks a TOTP or backup code, counting failures. */
    boolean verifyLoginCode(UUID userId, String code);
}
