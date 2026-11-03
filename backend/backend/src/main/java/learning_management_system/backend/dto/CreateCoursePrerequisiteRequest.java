package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import learning_management_system.backend.enums.PrerequisiteType;

import java.util.UUID;

/**
 * REQUIRED (default): completed before enrolling, with at least minGrade when set. COREQUISITE: completed or taken at
 * the same time. RECOMMENDED: shown, never enforced. minGrade is a letter grade A+ ... F.
 */
public record CreateCoursePrerequisiteRequest(@NotNull UUID prerequisiteCourseId, PrerequisiteType prerequisiteType,
                                              @Pattern(regexp = "^([A-D][+-]?|F)$") String minGrade) {}
