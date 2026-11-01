package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.EditingRoles;

/** PATCH a content item; null fields are left unchanged. Use the move endpoint to change its module. */
public record UpdateContentRequest(@Size(min = 3, max = 255) String contentTitle, String contentDescription, String contentBody,
                                   JsonNode contentHtml, @Positive Integer estimatedDurationMinutes,
                                   ContentAccessLevel accessLevel, EditingRoles editingRoles, LocalDate dateStart,
                                   LocalDate dateEnd, Boolean isLocked, Boolean isFrontPage, @Valid VideoLectureDto video) {}
