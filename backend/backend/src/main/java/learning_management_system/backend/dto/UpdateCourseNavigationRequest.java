package learning_management_system.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * The menu in the order wanted. Built-in tools left out keep their default place after the listed ones; an empty
 * list restores the defaults. External tools use toolKey "EXTERNAL_TOOL" with their externalToolId.
 */
public record UpdateCourseNavigationRequest(@NotNull @Valid List<Item> items) {

    public record Item(@NotNull @Size(max = 100) String toolKey, @Size(max = 100) String label, Boolean isHidden, UUID externalToolId) {}
}
