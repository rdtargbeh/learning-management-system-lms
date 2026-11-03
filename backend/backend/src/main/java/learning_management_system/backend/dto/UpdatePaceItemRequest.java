package learning_management_system.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Working days the step gets (0 = due the same day as the step before). */
public record UpdatePaceItemRequest(@NotNull @Min(0) @Max(365) Integer durationDays) {}
