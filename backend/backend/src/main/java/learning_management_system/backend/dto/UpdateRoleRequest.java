package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

/** PATCH a tenant role; permissions, when sent, replace the current set. */
public record UpdateRoleRequest(@Size(min = 3, max = 100) String roleName, String description, Boolean isActive,
                                List<String> permissions) {}
