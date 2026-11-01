package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CourseDto;
import learning_management_system.backend.dto.CreateCourseRequest;
import learning_management_system.backend.dto.UpdateCourseRequest;
import learning_management_system.backend.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** course. */
@Mapper(config = MapStructConfig.class)
public interface CourseMapper {

    /** Full view for course staff and administrators. */
    CourseDto toDto(Course course);

    /** View for students and observers: the self-enrollment code is not shown. */
    @Mapping(target = "selfEnrollmentCode", ignore = true)
    CourseDto toStudentDto(Course course);

    Course toEntity(CreateCourseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCourseRequest request, @MappingTarget Course course);
}
