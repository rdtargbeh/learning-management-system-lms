package learning_management_system.backend.dto;

import learning_management_system.backend.enums.PrerequisiteType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A course another course requires, recommends or must be taken with (table course_prerequisite). */
public record CoursePrerequisiteDto(UUID prerequisiteId, UUID courseId, UUID prerequisiteCourseId, String prerequisiteCourseCode,
                                    String prerequisiteCourseTitle, PrerequisiteType prerequisiteType, String minGrade,
                                    OffsetDateTime dateCreated) {}
