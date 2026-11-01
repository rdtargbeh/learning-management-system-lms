package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Move a section (with its enrollments) into another course. */
public record CrosslistSectionRequest(@NotNull UUID targetCourseId) {}
