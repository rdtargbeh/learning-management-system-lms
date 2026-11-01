package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** A student joins a course with the code the teacher shared. */
public record SelfEnrollRequest(@NotBlank String code) {}
