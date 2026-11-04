package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.PublicBrandingDto;
import learning_management_system.backend.dto.TenantBrandingDto;
import learning_management_system.backend.dto.TenantDto;
import learning_management_system.backend.dto.TenantSettingsDto;
import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.entity.Tenant;
import learning_management_system.backend.entity.TenantBranding;
import learning_management_system.backend.entity.TenantSettings;
import learning_management_system.backend.entity.TenantSubscription;
import learning_management_system.backend.entity.TenantUserLimit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** tenant, tenant_settings, tenant_branding, tenant_subscription and tenant_user_limit. */
@Mapper(config = MapStructConfig.class)
public interface TenantMapper {

    TenantDto toDto(Tenant tenant);

    TenantSettingsDto toDto(TenantSettings settings);

    /** PATCH: null values leave the setting unchanged. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TenantSettingsDto dto, @MappingTarget TenantSettings settings);

    TenantBrandingDto toDto(TenantBranding branding);

    /** PUT: the whole branding is replaced (null clears a value). */
    void replaceEntity(TenantBrandingDto dto, @MappingTarget TenantBranding branding);

    @Mapping(target = "tenantName", source = "tenant.tenantName")
    @Mapping(target = "tenantCode", source = "tenant.tenantCode")
    PublicBrandingDto toPublicDto(Tenant tenant, TenantBranding branding);

    TenantSubscriptionDto toDto(TenantSubscription subscription);

    void replaceEntity(UpsertTenantSubscriptionRequest request, @MappingTarget TenantSubscription subscription);

    TenantUserLimitDto toDto(TenantUserLimit limit);

    void replaceEntity(UpdateTenantUserLimitRequest request, @MappingTarget TenantUserLimit limit);
}
