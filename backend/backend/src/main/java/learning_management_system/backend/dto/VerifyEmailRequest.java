package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Confirm an email address with the token from the verification email. */
public record VerifyEmailRequest(@NotBlank String token) {}
