package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import learning_management_system.backend.enums.RoleScope;

/** Give a user a role, optionally scoped to one entity (course, org unit ...). */
public record AssignRoleRequest(@NotNull UUID roleId, RoleScope roleScope, UUID scopeEntityId, Boolean isPrimaryRole) {}
