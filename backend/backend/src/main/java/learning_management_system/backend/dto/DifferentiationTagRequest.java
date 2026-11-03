package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;

/** Creates a tag (tagName required) or changes one (PATCH: null leaves a value unchanged). */
public record DifferentiationTagRequest(@Size(min = 1, max = 100) String tagName, @Size(max = 2000) String tagDescription) {}
