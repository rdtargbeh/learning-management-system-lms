package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Set a new password with the token from the reset email. */
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 10, max = 200) String newPassword) {}
