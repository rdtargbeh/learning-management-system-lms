package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.StudentContentProgressStatus;

/** A student's progress on one content item (table student_content_progress). */
public record ContentProgressDto(UUID contentId, StudentContentProgressStatus status, BigDecimal percentComplete, Integer viewCount,
                                 OffsetDateTime dateFirstAccessed, OffsetDateTime dateLastAccessed, OffsetDateTime dateCompleted) {}
