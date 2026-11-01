package learning_management_system.backend.dto;

import java.util.List;
import java.util.UUID;
import learning_management_system.backend.enums.RoleLevel;

/** A role with its permission codes. shared = a platform-wide template (tenant_id NULL). */
public record RoleDto(UUID roleId, UUID tenantId, String roleName, String roleCode, RoleLevel roleLevel, String description,
                      boolean shared, Boolean isSystemRole, Boolean isActive, List<String> permissions) {}
