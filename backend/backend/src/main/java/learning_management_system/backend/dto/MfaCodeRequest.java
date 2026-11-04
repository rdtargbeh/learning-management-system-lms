package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** A 6-digit authenticator code. */
public record MfaCodeRequest(@NotBlank @Pattern(regexp = "\\d{6}") String code) {}
