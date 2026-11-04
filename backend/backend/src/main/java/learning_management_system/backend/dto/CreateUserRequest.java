package learning_management_system.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import learning_management_system.backend.enums.AuthProvider;
import learning_management_system.backend.enums.UserType;

/** Create an institution (tenant) user. Omit password to create an INVITED account. */
public record CreateUserRequest(@NotBlank @Size(min = 3, max = 100) String username,
                                @NotBlank @Email String email,
                                @NotNull UserType userType,
                                @Size(min = 10, max = 200) String password,
                                AuthProvider authProvider,
                                String sisSourceId,
                                @Valid UserProfileDto profile) {}
