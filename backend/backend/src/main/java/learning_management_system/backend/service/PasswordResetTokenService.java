package learning_management_system.backend.service;

import learning_management_system.backend.dto.ForgotPasswordRequest;
import learning_management_system.backend.dto.ResetPasswordRequest;

import java.net.InetAddress;

/** password_reset_token: "forgot password" by emailed single-use link. */
public interface PasswordResetTokenService {

    /** Mails a reset link when the account exists; behaves the same when it does not (no account discovery). */
    void forgot(ForgotPasswordRequest r, InetAddress ip, String userAgent);

    /** Sets the new password, ends every session and clears an automatic lock. */
    void reset(ResetPasswordRequest r);
}
