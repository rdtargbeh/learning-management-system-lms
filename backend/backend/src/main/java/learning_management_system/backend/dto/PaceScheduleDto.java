package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Projected due dates of a pace for one start date. compressed: durations were shortened to finish by the pace's end date. */
public record PaceScheduleDto(UUID paceId, String scope, UUID userId, LocalDate startDate, LocalDate endDate, boolean compressed, List<Step> items) {

    public record Step(UUID paceItemId, UUID moduleId, String itemType, UUID itemId, String title, Integer durationDays, LocalDate dueDate) {}
}
