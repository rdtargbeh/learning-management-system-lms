package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.AccommodationType;

/**
 * Approve an accommodation (disability services; ACCOMMODATION_MANAGE). courseId = null applies to every course.
 * isConfidential hides the details from instructors.
 */
public record CreateAccommodationRequest(UUID courseId, @NotNull AccommodationType accommodationType,
                                         @Min(1) @Max(500) Integer extendedTimePercentage, JsonNode details, LocalDate dateStart,
                                         LocalDate dateEnd, Boolean isConfidential) {}
