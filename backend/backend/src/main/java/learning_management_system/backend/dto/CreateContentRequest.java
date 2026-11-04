package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.EditingRoles;

/**
 * Create a content item (unpublished). typeCode is a content type code such as PAGE or VIDEO; moduleId places it
 * at the end of a module.
 */
public record CreateContentRequest(@NotBlank String typeCode, @NotBlank @Size(min = 3, max = 255) String contentTitle,
                                   String contentDescription, String contentBody, JsonNode contentHtml, UUID moduleId,
                                   @Positive Integer estimatedDurationMinutes, ContentAccessLevel accessLevel,
                                   EditingRoles editingRoles, LocalDate dateStart, LocalDate dateEnd, Boolean isFrontPage,
                                   @Valid VideoLectureDto video) {}
