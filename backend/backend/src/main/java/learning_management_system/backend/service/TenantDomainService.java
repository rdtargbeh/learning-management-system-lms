package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddTenantDomainRequest;
import learning_management_system.backend.dto.TenantDomainDto;

import java.util.List;
import java.util.UUID;

/**
 * tenant_domain: the web domains an institution uses (e.g. lms.example.edu). A new domain gets a verification token;
 * the institution publishes it as a DNS TXT record ({@code lms-verification=<token>}) and asks for verification.
 * Platform admins may add domains as verified, or mark them verified, after checking ownership themselves.
 * Only verified domains can become primary or be used to find an institution. tenantId null = the caller's own.
 */
public interface TenantDomainService {

    List<TenantDomainDto> list(UUID tenantId);

    TenantDomainDto add(UUID tenantId, AddTenantDomainRequest r);

    /** Looks up the TXT record; 409 DOMAIN_NOT_VERIFIED when it is not there. */
    TenantDomainDto verify(UUID tenantId, UUID domainId);

    /** Platform admins only: marks the domain verified without a DNS check. */
    TenantDomainDto markVerified(UUID tenantId, UUID domainId);

    TenantDomainDto makePrimary(UUID tenantId, UUID domainId);

    /** The primary domain cannot be removed while other domains exist; make another one primary first. */
    void remove(UUID tenantId, UUID domainId);

    /** The active institution that owns a verified domain (trusted lookup, no sign-in), or null. */
    UUID tenantForDomain(String domainName);
}
