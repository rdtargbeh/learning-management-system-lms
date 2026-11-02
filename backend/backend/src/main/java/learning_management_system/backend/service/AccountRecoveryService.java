package learning_management_system.backend.service;

import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;

import java.net.InetAddress;

/**
 * Forgotten passwords and email verification, by emailed single-use tokens (tables password_reset_token and
 * email_verification_token). Only token hashes are stored. Reset links last 30 minutes, verification links 24 hours.
 * "Forgot password" answers the same whether or not the account exists, so it cannot be used to discover accounts.
 */
public interface AccountRecoveryService {

    void forgotPassword(ForgotPasswordRequest r, InetAddress ip, String userAgent);

    /** Sets the new password, ends every session and unlocks the account. */
    void resetPassword(ResetPasswordRequest r);

    /** Emails the signed-in user a link to confirm their address. */
    void requestEmailVerification();

    void verifyEmail(String token);
}
