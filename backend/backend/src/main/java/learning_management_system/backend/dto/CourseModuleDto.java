package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleType;

/** A module of a course with its prerequisites and completion requirements (table course_module). */
public record CourseModuleDto(UUID moduleId, UUID courseId, UUID sectionId, Integer moduleNumber, String moduleTitle,
                              String moduleDescription, ModuleType moduleType, Integer displayOrder, LocalDate dateStart,
                              LocalDate dateEnd, Boolean isPublished, Boolean isLocked, Boolean requireSequentialProgress,
                              OffsetDateTime dateUnlock, List<UUID> prerequisiteModuleIds,
                              List<ModuleRequirementDto> requirements) {}
