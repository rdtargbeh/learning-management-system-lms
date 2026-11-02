package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Second sign-in step: the mfaToken from /auth/login and a 6-digit authenticator code or a backup code. */
public record MfaVerifyRequest(@NotBlank String mfaToken, @NotBlank String code) {}
