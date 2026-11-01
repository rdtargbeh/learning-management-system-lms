package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CourseInstructorDto;
import learning_management_system.backend.dto.CourseSectionDto;
import learning_management_system.backend.dto.CreateCourseSectionRequest;
import learning_management_system.backend.dto.UpdateCourseSectionRequest;
import learning_management_system.backend.entity.CourseInstructor;
import learning_management_system.backend.entity.CourseSection;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** course_section and course_instructor (section staff). */
@Mapper(config = MapStructConfig.class)
public interface CourseSectionMapper {

    CourseSectionDto toDto(CourseSection section);

    CourseSection toEntity(CreateCourseSectionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCourseSectionRequest request, @MappingTarget CourseSection section);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    CourseInstructorDto toDto(CourseInstructor staff, String username, String displayName);
}
