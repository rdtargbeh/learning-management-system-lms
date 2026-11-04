package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.enums.CourseLevel;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.CourseType;
import learning_management_system.backend.enums.DefaultPostPolicy;
import learning_management_system.backend.enums.GradeCalculationMethod;
import learning_management_system.backend.enums.HomePageType;

/** A course (table course). selfEnrollmentCode is only filled for course staff and administrators. */
public record CourseDto(UUID courseId, String courseCode, String courseTitle, String courseDescription, String syllabusHtml,
                        UUID termId, UUID categoryId, UUID orgUnitId, String sisSourceId, BigDecimal creditHours,
                        CourseLevel courseLevel, CourseType courseType, Integer totalSeats, CourseStatus courseStatus,
                        CourseKind courseKind, OffsetDateTime dateStart, OffsetDateTime dateEnd, String timeZone,
                        HomePageType homePageType, String courseImageUrl, Boolean allowSelfEnrollment,
                        String selfEnrollmentCode, Boolean isPublicSyllabus, String contentLicense,
                        GradeCalculationMethod gradeCalculationMethod, DefaultPostPolicy defaultPostPolicy,
                        Boolean isArchived, OffsetDateTime dateCreated) {}
