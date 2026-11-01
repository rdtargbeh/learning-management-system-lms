package learning_management_system.backend.service;

import learning_management_system.backend.dto.CourseCategoryDto;
import learning_management_system.backend.dto.CreateCourseCategoryRequest;
import learning_management_system.backend.dto.UpdateCourseCategoryRequest;

import java.util.List;
import java.util.UUID;

/** Course catalog categories (nested). Everyone in the tenant can read them. */
public interface CourseCategoryService {

    List<CourseCategoryDto> list();

    CourseCategoryDto create(CreateCourseCategoryRequest r);

    CourseCategoryDto update(UUID categoryId, UpdateCourseCategoryRequest r);
}
