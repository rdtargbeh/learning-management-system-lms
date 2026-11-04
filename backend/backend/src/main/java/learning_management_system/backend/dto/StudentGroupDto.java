package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

/** A group of students (table student_group). */
public record StudentGroupDto(UUID groupId, UUID groupSetId, UUID sectionId, String groupName, String groupDescription,
                              Integer maxMembers, Integer currentMembers, LocalDate dateStart, LocalDate dateEnd, Boolean isActive) {}
