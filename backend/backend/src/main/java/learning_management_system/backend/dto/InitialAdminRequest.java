package learning_management_system.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The first tenant administrator created with a new institution. */
public record InitialAdminRequest(@NotBlank @Size(min = 3, max = 100) String username, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 10, max = 200) String password, String firstName, String lastName) {}
