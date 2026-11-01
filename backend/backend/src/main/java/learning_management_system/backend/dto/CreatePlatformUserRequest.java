package learning_management_system.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.PlatformRole;

/** Create a platform (system) user; tenant_id stays NULL. */
public record CreatePlatformUserRequest(@NotBlank @Size(min = 3, max = 100) String username, @NotBlank @Email String email,
                                        @NotBlank @Size(min = 12, max = 200) String password,
                                        @NotNull PlatformRole platformRole,
                                        String jobTitle, String department, Boolean canImpersonate, @Valid UserProfileDto profile) {}
