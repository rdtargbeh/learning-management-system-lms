package learning_management_system.backend.dto;

import learning_management_system.backend.enums.PermissionType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A grant (or, with isGranted false, a denial) of one permission on a content item to a user or a role. */
public record ContentPermissionDto(UUID permissionId, UUID contentId, UUID userId, UUID roleId, PermissionType permissionType, Boolean isGranted,
                                   OffsetDateTime dateGranted, OffsetDateTime dateExpires, UUID grantedBy, boolean active) {}
