package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.CourseInstructorRole;

/** A staff member of a section (table course_instructor) with their name. */
public record CourseInstructorDto(UUID courseInstructorId, UUID sectionId, UUID userId, String username, String displayName,
                                  CourseInstructorRole role, Boolean isPrimaryInstructor, LocalDate dateAssigned,
                                  LocalDate dateStart, LocalDate dateEnd) {}
