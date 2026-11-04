package learning_management_system.backend.service;

/** email_verification_token: confirming that a user owns their email address. */
public interface EmailVerificationTokenService {

    /** Mails a link to the signed-in user's address (409 when it is already verified). */
    void request();

    /** Marks the address verified (no sign-in needed). */
    void verify(String token);
}
