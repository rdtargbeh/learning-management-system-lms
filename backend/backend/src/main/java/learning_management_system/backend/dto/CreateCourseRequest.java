package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.enums.CourseLevel;
import learning_management_system.backend.enums.CourseType;

/**
 * Create a course. By default a first section ("01") is created too, like Canvas's default section;
 * instructorUserId makes that user its primary instructor.
 */
public record CreateCourseRequest(@NotBlank @Size(min = 3, max = 50) String courseCode,
                                  @NotBlank @Size(min = 3, max = 255) String courseTitle,
                                  String courseDescription, UUID termId, UUID categoryId, UUID orgUnitId, String sisSourceId,
                                  @Positive BigDecimal creditHours, CourseLevel courseLevel, CourseType courseType,
                                  CourseKind courseKind, OffsetDateTime dateStart, OffsetDateTime dateEnd, String timeZone,
                                  Boolean createDefaultSection, @Positive Integer defaultSectionCapacity,
                                  UUID instructorUserId) {}
