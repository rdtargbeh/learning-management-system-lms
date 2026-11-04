package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A web domain of an institution (table tenant_domain). Until it is verified, {@code verificationRecord} is the DNS TXT
 * record the institution adds to prove it owns the domain.
 */
public record TenantDomainDto(UUID domainId, UUID tenantId, String domainName, Boolean isPrimary, Boolean isVerified,
                              String verificationRecord, OffsetDateTime dateVerified, OffsetDateTime dateCreated) {}
