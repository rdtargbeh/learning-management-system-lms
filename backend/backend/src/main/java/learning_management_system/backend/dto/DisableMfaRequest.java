package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Turning MFA off needs the password and a current code. */
public record DisableMfaRequest(@NotBlank String password, @NotBlank String code) {}
