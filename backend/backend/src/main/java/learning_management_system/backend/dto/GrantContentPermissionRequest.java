package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import learning_management_system.backend.enums.PermissionType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Gives (isGranted true, the default) or takes away (false) one permission on an item, for one user or one role.
 * VIEW grants open RESTRICTED items; a VIEW denial hides any item from that user.
 */
public record GrantContentPermissionRequest(UUID userId, UUID roleId, @NotNull PermissionType permissionType, Boolean isGranted,
                                            OffsetDateTime dateExpires) {}
