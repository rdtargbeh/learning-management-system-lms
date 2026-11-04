package learning_management_system.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Create a course category. */
public record CreateCourseCategoryRequest(@NotBlank @Size(min = 3, max = 100) String categoryName, @Size(max = 50) String categoryCode,
                                          String description, UUID parentCategoryId, Integer displayOrder) {}
