package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The signed-in user changes their own password. */
public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 10, max = 200) String newPassword) {}
