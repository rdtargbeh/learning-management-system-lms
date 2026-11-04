package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.CreateCourseModuleRequest;
import learning_management_system.backend.dto.ModuleRequirementDto;
import learning_management_system.backend.dto.UpdateCourseModuleRequest;
import learning_management_system.backend.entity.CourseModule;
import learning_management_system.backend.entity.ModuleCompletionRequirement;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.UUID;

/** course_module and module_completion_requirement. */
@Mapper(config = MapStructConfig.class)
public interface CourseModuleMapper {

    @Mapping(target = "prerequisiteModuleIds", source = "prerequisiteModuleIds")
    @Mapping(target = "requirements", source = "requirements")
    CourseModuleDto toDto(CourseModule module, List<UUID> prerequisiteModuleIds, List<ModuleRequirementDto> requirements);

    ModuleRequirementDto toDto(ModuleCompletionRequirement requirement);

    CourseModule toEntity(CreateCourseModuleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateCourseModuleRequest request, @MappingTarget CourseModule module);
}
