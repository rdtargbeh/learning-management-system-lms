package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.ModuleType;

/**
 * Create a module (unpublished, added at the end). dateUnlock keeps it locked for students until then; sectionId
 * limits it to one section.
 */
public record CreateCourseModuleRequest(@NotBlank @Size(min = 3, max = 255) String moduleTitle, String moduleDescription,
                                        ModuleType moduleType, UUID sectionId, LocalDate dateStart, LocalDate dateEnd,
                                        OffsetDateTime dateUnlock, Boolean requireSequentialProgress) {}
