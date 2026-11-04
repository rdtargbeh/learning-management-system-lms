package learning_management_system.backend.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Shared settings for every MapStruct mapper: mappers are Spring beans, and entity columns that a DTO does not carry
 * (ids, tenant_id, audit columns) are simply left alone.
 */
@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapStructConfig {
}
