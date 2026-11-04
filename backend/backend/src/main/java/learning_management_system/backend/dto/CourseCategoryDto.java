package learning_management_system.backend.dto;

import java.util.UUID;

/** A course catalog category (table course_category). */
public record CourseCategoryDto(UUID categoryId, UUID parentCategoryId, String categoryName, String categoryCode,
                                String description, Integer displayOrder, Boolean isActive) {}
