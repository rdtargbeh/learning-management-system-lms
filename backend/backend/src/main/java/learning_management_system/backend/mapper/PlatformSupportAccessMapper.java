package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.PlatformSupportAccessDto;
import learning_management_system.backend.entity.PlatformSupportAccess;
import org.mapstruct.Mapper;

/** platform_support_access. */
@Mapper(config = MapStructConfig.class)
public interface PlatformSupportAccessMapper {

    PlatformSupportAccessDto toDto(PlatformSupportAccess access);
}
