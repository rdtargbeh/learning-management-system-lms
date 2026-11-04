package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.TenantDomainDto;
import learning_management_system.backend.entity.TenantDomain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** tenant_domain. */
@Mapper(config = MapStructConfig.class)
public interface TenantDomainMapper {

    String TXT_PREFIX = "lms-verification=";

    @Mapping(target = "verificationRecord", expression = "java(verificationRecord(domain))")
    TenantDomainDto toDto(TenantDomain domain);

    default String verificationRecord(TenantDomain d) {
        return Boolean.TRUE.equals(d.getIsVerified()) || d.getVerificationToken() == null ? null : TXT_PREFIX + d.getVerificationToken();
    }
}
