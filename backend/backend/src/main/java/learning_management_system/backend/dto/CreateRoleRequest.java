package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Create a tenant role. */
public record CreateRoleRequest(@NotBlank @Size(min = 3, max = 100) String roleName, @Size(max = 100) String roleCode,
                                String description, List<String> permissions) {}
