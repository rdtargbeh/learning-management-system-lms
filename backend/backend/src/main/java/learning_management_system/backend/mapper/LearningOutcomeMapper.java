package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.LearningOutcomeDto;
import learning_management_system.backend.dto.LearningOutcomeRequest;
import learning_management_system.backend.entity.LearningOutcome;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** learning_outcome. */
@Mapper(config = MapStructConfig.class)
public interface LearningOutcomeMapper {

    @Mapping(target = "owner", expression = "java(o.getCourseId() != null ? \"COURSE\" : \"ORG_UNIT\")")
    LearningOutcomeDto toDto(LearningOutcome o);

    /** PATCH: null values leave the field unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LearningOutcomeRequest r, @MappingTarget LearningOutcome o);
}
