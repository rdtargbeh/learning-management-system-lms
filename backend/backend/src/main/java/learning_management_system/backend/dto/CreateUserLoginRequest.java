package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Add a sign-in name; it then works like the username at /auth/login. */
public record CreateUserLoginRequest(@NotBlank @Size(max = 255) String uniqueLogin, @Size(max = 255) String sisSourceId,
                                     @Size(max = 255) String integrationId) {}
