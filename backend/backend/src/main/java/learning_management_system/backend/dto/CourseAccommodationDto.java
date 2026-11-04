package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.AccommodationType;

/** An accommodation of a student in the course, for the course staff. details is null when confidential. */
public record CourseAccommodationDto(UUID accommodationId, UUID userId, String username, String displayName,
                                     AccommodationType accommodationType, Integer extendedTimePercentage, JsonNode details,
                                     LocalDate dateStart, LocalDate dateEnd) {}
