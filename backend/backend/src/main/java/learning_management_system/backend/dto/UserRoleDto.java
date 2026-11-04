package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.RoleScope;

/** A role assignment of a user. */
public record UserRoleDto(UUID userRoleId, UUID userId, UUID roleId, String roleName, RoleScope roleScope,
                          UUID scopeEntityId, Boolean isPrimaryRole, LocalDate dateAssigned) {}
