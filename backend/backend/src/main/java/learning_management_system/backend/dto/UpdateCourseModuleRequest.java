package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import learning_management_system.backend.enums.ModuleType;

/** PATCH a module; null fields are left unchanged. isLocked = true locks it for students. */
public record UpdateCourseModuleRequest(@Size(min = 3, max = 255) String moduleTitle, String moduleDescription, ModuleType moduleType,
                                        LocalDate dateStart, LocalDate dateEnd, OffsetDateTime dateUnlock, Boolean isLocked,
                                        Boolean requireSequentialProgress) {}
