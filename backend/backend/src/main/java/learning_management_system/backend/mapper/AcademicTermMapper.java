package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.AcademicTermDto;
import learning_management_system.backend.dto.CreateAcademicTermRequest;
import learning_management_system.backend.dto.UpdateAcademicTermRequest;
import learning_management_system.backend.entity.AcademicTerm;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** academic_term. */
@Mapper(config = MapStructConfig.class)
public interface AcademicTermMapper {

    AcademicTermDto toDto(AcademicTerm term);

    AcademicTerm toEntity(CreateAcademicTermRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAcademicTermRequest request, @MappingTarget AcademicTerm term);
}
