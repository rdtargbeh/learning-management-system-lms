package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/** The full new order: every id, first to last. */
public record ReorderRequest(@NotEmpty List<UUID> orderedIds) {}
