package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** Standard courses to associate with the blueprint; content arrives with the next sync. */
public record AssociateBlueprintCoursesRequest(@NotEmpty List<UUID> courseIds) {}
