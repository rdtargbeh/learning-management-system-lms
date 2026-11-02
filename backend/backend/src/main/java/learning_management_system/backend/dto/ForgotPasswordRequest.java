package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Ask for a password-reset link. Institution users give their institution's code; login = username or email. */
public record ForgotPasswordRequest(String tenantCode, @NotBlank String login) {}
