package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Turn self-enrollment by code on or off. regenerateCode issues a new code. */
public record SelfEnrollmentSettingsRequest(@NotNull Boolean enabled, Boolean regenerateCode) {}
