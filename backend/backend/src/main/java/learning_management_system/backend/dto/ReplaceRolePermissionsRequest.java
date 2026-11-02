package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** The complete list of permission codes a role should grant (an empty list removes all). */
public record ReplaceRolePermissionsRequest(@NotNull List<String> permissions) {}
