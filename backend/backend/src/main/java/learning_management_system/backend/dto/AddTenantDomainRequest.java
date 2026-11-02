package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Adds a domain such as lms.example.edu. {@code verified} is honoured only for platform admins (they check ownership themselves). */
public record AddTenantDomainRequest(@NotBlank @Size(max = 255) String domainName, Boolean verified) {}
