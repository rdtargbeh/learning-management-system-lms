package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.TenantSettings;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** tenant and tenant_settings. */
@Mapper(config = MapStructConfig.class)
public interface TenantMapper {

    TenantDto toDto(Tenant tenant);

    TenantSettingsDto toDto(TenantSettings settings);

    /** PATCH: null values leave the setting unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TenantSettingsDto dto, @MappingTarget TenantSettings settings);
}
