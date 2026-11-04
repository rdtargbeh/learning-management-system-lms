package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Creates a pace (sectionId or userId for a section or student pace, neither for the course default) or changes its
 * settings (PATCH; the scope cannot change). blackoutDates: JSON array of "YYYY-MM-DD" days without work. dateEnd:
 * the schedule is compressed to finish by then.
 */
public record CoursePaceRequest(UUID sectionId, UUID userId, Boolean excludeWeekends, JsonNode blackoutDates, OffsetDateTime dateEnd) {}
