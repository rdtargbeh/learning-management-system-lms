package learning_management_system.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.enums.CourseLevel;
import learning_management_system.backend.enums.CourseType;
import learning_management_system.backend.enums.DefaultPostPolicy;
import learning_management_system.backend.enums.GradeCalculationMethod;
import learning_management_system.backend.enums.HomePageType;

/**
 * PATCH a course; null fields are left unchanged. Catalog fields (code, term, category, org unit, SIS id, credits,
 * seats, kind) need COURSE_MANAGE; teachers can change the rest.
 */
public record UpdateCourseRequest(@Size(min = 3, max = 50) String courseCode, @Size(min = 3, max = 255) String courseTitle,
                                  String courseDescription, String syllabusHtml, UUID termId, UUID categoryId, UUID orgUnitId,
                                  String sisSourceId, @Positive BigDecimal creditHours, CourseLevel courseLevel,
                                  CourseType courseType, @Positive Integer totalSeats, CourseKind courseKind,
                                  OffsetDateTime dateStart, OffsetDateTime dateEnd, String timeZone, HomePageType homePageType,
                                  String courseImageUrl, Boolean isPublicSyllabus, String contentLicense,
                                  GradeCalculationMethod gradeCalculationMethod, DefaultPostPolicy defaultPostPolicy) {}
