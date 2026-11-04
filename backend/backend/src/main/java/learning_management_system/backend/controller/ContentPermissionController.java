package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ContentPermissionDto;
import learning_management_system.backend.dto.GrantContentPermissionRequest;
import learning_management_system.backend.enums.PermissionType;
import learning_management_system.backend.service.ContentPermissionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** content_permission: per-user and per-role permissions on a content item. */
@RestController
@RequestMapping("/api/v1/content/{contentId}/permissions")
public class ContentPermissionController {
    private final ContentPermissionService permissions;

    public ContentPermissionController(ContentPermissionService permissions) { this.permissions = permissions; }

    @GetMapping
    public List<ContentPermissionDto> list(@PathVariable UUID contentId) { return permissions.list(contentId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentPermissionDto grant(@PathVariable UUID contentId, @Valid @RequestBody GrantContentPermissionRequest r) { return permissions.grant(contentId, r); }

    @DeleteMapping("/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID contentId, @PathVariable UUID permissionId) { permissions.revoke(contentId, permissionId); }

    @GetMapping("/mine")
    public Set<PermissionType> mine(@PathVariable UUID contentId) { return permissions.mine(contentId); }
}
