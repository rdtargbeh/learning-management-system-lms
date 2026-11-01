package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Sign-in request. Platform staff omit tenantCode; institution users give their institution's code. login = username or email. */
public record LoginRequest(String tenantCode, @NotBlank String login, @NotBlank String password) {}
