package learning_management_system.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ContentAccessLevel;
import learning_management_system.backend.enums.EditingRoles;

/**
 * A content item: page, video, file, link ... (table content). contentBody is the HTML body. For a student, a
 * locked item comes without its body and lockReason says why.
 */
public record ContentDto(UUID contentId, UUID courseId, UUID moduleId, String typeCode, String contentTitle, String contentDescription,
                         String contentBody, JsonNode contentHtml, Boolean isPublished, Boolean isLocked, Boolean isFrontPage,
                         EditingRoles editingRoles, Integer displayOrder, Integer estimatedDurationMinutes,
                         ContentAccessLevel accessLevel, OffsetDateTime datePublished, LocalDate dateStart, LocalDate dateEnd,
                         VideoLectureDto video, OffsetDateTime dateUpdated, String lockReason) {}
