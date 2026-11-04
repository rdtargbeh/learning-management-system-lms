package learning_management_system.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH of the institution record: null leaves a value unchanged. institutionCode can be changed by platform admins only. */
public record UpdateInstitutionRequest(@Size(min = 3, max = 255) String institutionName,
                                       @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String institutionCode,
                                       @Size(max = 100) String country, @Min(1800) @Max(2100) Integer establishedYear,
                                       @Size(max = 50) String institutionType,
                                       @Size(max = 500) @Pattern(regexp = "^https?://.+") String websiteUrl,
                                       @Size(max = 500) @Pattern(regexp = "^https?://.+") String logoUrl) {}
