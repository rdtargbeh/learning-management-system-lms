package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CourseCategoryDto;
import learning_management_system.backend.dto.CreateCourseCategoryRequest;
import learning_management_system.backend.dto.UpdateCourseCategoryRequest;
import learning_management_system.backend.entity.CourseCategory;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.CourseCategoryMapper;
import learning_management_system.backend.repository.CourseCategoryRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.CourseCategoryService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CourseCategoryServiceImplementation implements CourseCategoryService {
    private final CourseCategoryRepository categories;
    private final CourseCategoryMapper categoryMapper;
    private final EntityRefresher refresher;

    public CourseCategoryServiceImplementation(CourseCategoryRepository categories, CourseCategoryMapper categoryMapper,
                                               EntityRefresher refresher) {
        this.categories = categories;
        this.categoryMapper = categoryMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseCategoryDto> list() {
        Access.requireTenant();
        return categories.findAllByOrderByDisplayOrderAscCategoryNameAsc().stream().map(categoryMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CourseCategoryDto create(CreateCourseCategoryRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        if (r.parentCategoryId() != null) load(r.parentCategoryId());
        CourseCategory c = categoryMapper.toEntity(r);
        c.setCategoryName(r.categoryName().trim());
        return categoryMapper.toDto(refresher.refresh(categories.saveAndFlush(c)));
    }

    @Transactional
    @Override
    public CourseCategoryDto update(UUID categoryId, UpdateCourseCategoryRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        CourseCategory c = load(categoryId);
        if (r.parentCategoryId() != null) {
            Set<UUID> seen = new HashSet<>();
            for (UUID id = r.parentCategoryId(); id != null; id = load(id).getParentCategoryId()) {
                if (id.equals(categoryId) || !seen.add(id)) {
                    throw ApiException.badRequest("CATEGORY_CYCLE", "A category cannot be placed under itself or one of its children");
                }
            }
        }
        categoryMapper.updateEntity(r, c);
        return categoryMapper.toDto(categories.saveAndFlush(c));
    }

    private CourseCategory load(UUID id) {
        return categories.findById(id).orElseThrow(() -> ApiException.notFound("Course category"));
    }
}
