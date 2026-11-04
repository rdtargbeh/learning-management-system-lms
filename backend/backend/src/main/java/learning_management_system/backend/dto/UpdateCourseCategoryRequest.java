package learning_management_system.backend.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/** PATCH a course category; null fields are left unchanged. */
public record UpdateCourseCategoryRequest(@Size(min = 3, max = 100) String categoryName, @Size(max = 50) String categoryCode,
                                          String description, UUID parentCategoryId, Integer displayOrder, Boolean isActive) {}
