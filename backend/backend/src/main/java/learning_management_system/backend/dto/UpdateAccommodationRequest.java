package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/** PATCH an accommodation; null fields are left unchanged. */
public record UpdateAccommodationRequest(@Min(1) @Max(500) Integer extendedTimePercentage, JsonNode details, LocalDate dateStart,
                                         LocalDate dateEnd, Boolean isActive, Boolean isConfidential) {}
