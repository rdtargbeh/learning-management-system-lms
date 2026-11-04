package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** An administrator sets a user's password. */
public record SetPasswordRequest(@NotBlank @Size(min = 10, max = 200) String newPassword, Boolean mustChangePassword) {}
