package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.CourseSectionStatus;
import learning_management_system.backend.enums.DeliveryMode;

/**
 * A section of a course (table course_section). crosslistedFromCourseId is set when the section was moved in from
 * another course.
 */
public record CourseSectionDto(UUID sectionId, UUID courseId, UUID crosslistedFromCourseId, UUID termId, String sectionNumber,
                               String sisSourceId, Integer capacity, DeliveryMode deliveryMode, Integer enrolledCount,
                               Integer waitlistCount, LocalDate dateStart, LocalDate dateEnd, LocalDate registrationOpenDate,
                               LocalDate registrationCloseDate, Boolean isOpenForEnrollment, CourseSectionStatus status) {}
