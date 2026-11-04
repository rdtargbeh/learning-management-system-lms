package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AccommodationType;

/** An approved accommodation for a student, for all courses or one course (table accommodation). */
public record AccommodationDto(UUID accommodationId, UUID userId, UUID courseId, AccommodationType accommodationType,
                               Integer extendedTimePercentage, JsonNode details, UUID approvedBy, OffsetDateTime dateApproved,
                               LocalDate dateStart, LocalDate dateEnd, Boolean isActive, Boolean isConfidential) {}
