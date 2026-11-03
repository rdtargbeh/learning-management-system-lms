package learning_management_system.backend.service;

import learning_management_system.backend.dto.ContentPermissionDto;
import learning_management_system.backend.dto.GrantContentPermissionRequest;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.enums.PermissionType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * content_permission: per-user and per-role permissions on content items. Students can view ENROLLED and PUBLIC items
 * (and download, print and comment on them) unless denied; RESTRICTED items need a VIEW grant; INSTRUCTOR_ONLY items
 * are never shown to students. A user's own denial wins over any grant. The course's teachers manage permissions.
 */
public interface ContentPermissionService {

    List<ContentPermissionDto> list(UUID contentId);

    ContentPermissionDto grant(UUID contentId, GrantContentPermissionRequest r);

    void revoke(UUID contentId, UUID permissionId);

    /** The caller's effective permissions on the item (staff have all). */
    Set<PermissionType> mine(UUID contentId);

    /** Internal: may this student see the item (published state and modules are checked elsewhere)? */
    boolean canView(Content content, UUID userId);
}
