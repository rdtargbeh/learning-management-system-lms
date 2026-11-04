package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Trade a refresh token for new tokens (the old refresh token stops working). */
public record RefreshRequest(@NotBlank String refreshToken) {}
