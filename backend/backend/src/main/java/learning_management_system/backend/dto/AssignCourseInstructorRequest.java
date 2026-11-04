package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import learning_management_system.backend.enums.CourseInstructorRole;

/** Add a staff member to a section. PRIMARY makes them the section's primary instructor (one per section). */
public record AssignCourseInstructorRequest(@NotNull UUID userId, @NotNull CourseInstructorRole role, LocalDate dateStart,
                                            LocalDate dateEnd) {}
