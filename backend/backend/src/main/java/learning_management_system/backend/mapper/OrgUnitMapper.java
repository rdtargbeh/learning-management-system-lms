package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.CreateOrgUnitRequest;
import learning_management_system.backend.dto.OrgUnitDto;
import learning_management_system.backend.dto.UpdateOrgUnitRequest;
import learning_management_system.backend.entity.OrgUnit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** org_unit. */
@Mapper(config = MapStructConfig.class)
public interface OrgUnitMapper {

    OrgUnitDto toDto(OrgUnit orgUnit);

    OrgUnit toEntity(CreateOrgUnitRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateOrgUnitRequest request, @MappingTarget OrgUnit orgUnit);
}
