package learning_management_system.backend.controller;

import learning_management_system.backend.dto.PermissionDto;
import learning_management_system.backend.service.PermissionService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/** permissions: the catalogue of permission codes. */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    private final PermissionService permissions;

    public PermissionController(PermissionService permissions) { this.permissions = permissions; }

    @GetMapping
    public List<PermissionDto> list() { return permissions.list(); }
}
