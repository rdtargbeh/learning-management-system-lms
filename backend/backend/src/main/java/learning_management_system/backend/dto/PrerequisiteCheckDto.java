package learning_management_system.backend.dto;

import java.util.List;
import java.util.UUID;

/** Whether a user meets a course's enforced prerequisites; {@code items} covers every prerequisite, recommended ones too. */
public record PrerequisiteCheckDto(UUID courseId, UUID userId, boolean met, List<Item> items) {

    /** status: COMPLETED, ENROLLED (taking it now), GRADE_TOO_LOW, GRADE_UNKNOWN or MISSING. */
    public record Item(UUID prerequisiteCourseId, String courseCode, String courseTitle, String prerequisiteType, String minGrade,
                       String status, String grade, boolean met) {}
}
