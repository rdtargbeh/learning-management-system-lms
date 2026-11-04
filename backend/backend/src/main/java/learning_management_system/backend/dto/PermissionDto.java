package learning_management_system.backend.dto;

import java.util.UUID;
import learning_management_system.backend.enums.Action;

/** A permission code that roles can grant. */
public record PermissionDto(UUID permissionId, String permissionCode, String permissionName, String resourceType,
                            Action action) {}
