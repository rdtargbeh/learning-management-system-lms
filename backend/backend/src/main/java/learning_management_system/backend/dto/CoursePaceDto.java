package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.enums.PaceStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** A course pace (table course_pace): scope COURSE (default), SECTION or STUDENT. blackoutDates: JSON array of "YYYY-MM-DD". */
public record CoursePaceDto(UUID paceId, UUID courseId, String scope, UUID sectionId, UUID userId, PaceStatus paceStatus, Boolean excludeWeekends,
                            JsonNode blackoutDates, OffsetDateTime dateEnd, OffsetDateTime datePublished, List<CoursePaceItemDto> items) {}
