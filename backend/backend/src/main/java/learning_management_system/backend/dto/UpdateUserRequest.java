package learning_management_system.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** PATCH a user account; null fields are left unchanged. */
public record UpdateUserRequest(@Size(min = 3, max = 100) String username, @Email String email, String sisSourceId,
                                Boolean mfaRequired) {}
