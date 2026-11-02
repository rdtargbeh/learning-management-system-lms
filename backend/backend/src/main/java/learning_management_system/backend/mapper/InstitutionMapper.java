package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.InstitutionDto;
import learning_management_system.backend.dto.UpdateInstitutionRequest;
import learning_management_system.backend.entity.Institution;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** institution. */
@Mapper(config = MapStructConfig.class)
public interface InstitutionMapper {

    InstitutionDto toDto(Institution institution);

    /** PATCH: null values leave the field unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateInstitutionRequest request, @MappingTarget Institution institution);
}
