package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CourseCategoryDto;
import learning_management_system.backend.dto.CreateCourseCategoryRequest;
import learning_management_system.backend.dto.UpdateCourseCategoryRequest;
import learning_management_system.backend.entity.CourseCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** course_category. */
@Mapper(config = MapStructConfig.class)
public interface CourseCategoryMapper {

    CourseCategoryDto toDto(CourseCategory category);

    CourseCategory toEntity(CreateCourseCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCourseCategoryRequest request, @MappingTarget CourseCategory category);
}
