package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Add an institution-specific content type. */
public record CreateContentTypeRequest(@NotBlank @Size(min = 3, max = 100) String typeName,
                                       @NotBlank @Pattern(regexp = "^[A-Za-z0-9_]{2,50}$") String typeCode,
                                       String description, @Size(max = 500) String iconUrl) {}
