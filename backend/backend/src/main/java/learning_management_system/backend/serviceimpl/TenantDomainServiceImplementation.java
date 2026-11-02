package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddTenantDomainRequest;
import learning_management_system.backend.dto.TenantDomainDto;
import learning_management_system.backend.entity.TenantDomain;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantDomainMapper;
import learning_management_system.backend.repository.TenantDomainRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.TenantDomainService;
import learning_management_system.backend.utility.DnsTxtResolver;
import learning_management_system.backend.utility.EntityRefresher;
import learning_management_system.backend.utility.Tokens;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TenantDomainServiceImplementation implements TenantDomainService {
    private static final Pattern DOMAIN = Pattern.compile("^[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?)+$");

    private final TenantDomainRepository domains;
    private final TenantRepository tenants;
    private final TenantDomainMapper mapper;
    private final DnsTxtResolver dns;
    private final SystemTx sys;
    private final EntityRefresher refresher;
    private final AuditLogService audit;

    public TenantDomainServiceImplementation(TenantDomainRepository domains, TenantRepository tenants, TenantDomainMapper mapper,
                                             DnsTxtResolver dns, SystemTx sys, EntityRefresher refresher, AuditLogService audit) {
        this.domains = domains;
        this.tenants = tenants;
        this.mapper = mapper;
        this.dns = dns;
        this.sys = sys;
        this.refresher = refresher;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TenantDomainDto> list(UUID tenantId) {
        UUID t = target(tenantId);
        return domains.findByTenantIdOrderByIsPrimaryDescDomainNameAsc(t).stream().map(mapper::toDto).toList();
    }

    @Transactional
    @Override
    public TenantDomainDto add(UUID tenantId, AddTenantDomainRequest r) {
        UUID t = target(tenantId);
        String name = normalise(r.domainName());
        // domain names are unique across all institutions, so the check must see every tenant
        if (sys.platform(() -> domains.findByDomainNameIgnoreCase(name).isPresent())) {
            throw ApiException.conflict("DOMAIN_TAKEN", "This domain is already registered");
        }
        boolean preVerified = Boolean.TRUE.equals(r.verified());
        if (preVerified && !Access.ctx().platformAdmin()) throw ApiException.forbidden("Only platform admins add verified domains");
        List<TenantDomain> existing = domains.findByTenantIdOrderByIsPrimaryDescDomainNameAsc(t);
        TenantDomain d = new TenantDomain();
        d.setTenantId(t);
        d.setDomainName(name);
        d.setIsPrimary(existing.isEmpty());                  // the first domain is the primary one
        d.setIsVerified(preVerified);
        if (preVerified) d.setDateVerified(OffsetDateTime.now());
        else d.setVerificationToken(Tokens.random().substring(0, 32));
        TenantDomain saved = refresher.refresh(domains.saveAndFlush(d));
        audit.record("DOMAIN_ADDED", "TENANT_DOMAIN", saved.getDomainId(), null, Severity.INFO, null, Map.of("domainName", name));
        return mapper.toDto(saved);
    }

    @Transactional
    @Override
    public TenantDomainDto verify(UUID tenantId, UUID domainId) {
        TenantDomain d = load(target(tenantId), domainId);
        if (Boolean.TRUE.equals(d.getIsVerified())) return mapper.toDto(d);
        String expected = TenantDomainMapper.TXT_PREFIX + d.getVerificationToken();
        boolean found = dns.txtRecords(d.getDomainName()).stream().anyMatch(expected::equals)
                || dns.txtRecords("_lms-verification." + d.getDomainName()).stream().anyMatch(expected::equals);
        if (!found) {
            throw ApiException.conflict("DOMAIN_NOT_VERIFIED", "No DNS TXT record \"" + expected + "\" found on " + d.getDomainName()
                    + " (DNS changes can take a while to appear)");
        }
        return mapper.toDto(markVerified(d));
    }

    @Transactional
    @Override
    public TenantDomainDto markVerified(UUID tenantId, UUID domainId) {
        Access.requirePlatformAdmin();
        TenantDomain d = load(target(tenantId), domainId);
        return mapper.toDto(Boolean.TRUE.equals(d.getIsVerified()) ? d : markVerified(d));
    }

    @Transactional
    @Override
    public TenantDomainDto makePrimary(UUID tenantId, UUID domainId) {
        UUID t = target(tenantId);
        TenantDomain d = load(t, domainId);
        if (Boolean.TRUE.equals(d.getIsPrimary())) return mapper.toDto(d);
        if (!Boolean.TRUE.equals(d.getIsVerified())) throw ApiException.conflict("DOMAIN_NOT_VERIFIED", "Verify the domain before making it primary");
        domains.clearPrimary(t);
        d = load(t, domainId);
        d.setIsPrimary(true);
        TenantDomain saved = domains.saveAndFlush(d);
        audit.record("DOMAIN_MADE_PRIMARY", "TENANT_DOMAIN", saved.getDomainId(), null, Severity.INFO, null, null);
        return mapper.toDto(saved);
    }

    @Transactional
    @Override
    public void remove(UUID tenantId, UUID domainId) {
        UUID t = target(tenantId);
        TenantDomain d = load(t, domainId);
        if (Boolean.TRUE.equals(d.getIsPrimary()) && domains.findByTenantIdOrderByIsPrimaryDescDomainNameAsc(t).size() > 1) {
            throw ApiException.conflict("PRIMARY_DOMAIN", "Make another domain primary before removing this one");
        }
        domains.delete(d);
        audit.record("DOMAIN_REMOVED", "TENANT_DOMAIN", domainId, null, Severity.WARNING, Map.of("domainName", d.getDomainName()), null);
    }

    @Override
    public UUID tenantForDomain(String domainName) {
        String name = domainName == null ? "" : domainName.trim().toLowerCase();
        return sys.platform(() -> domains.findByDomainNameIgnoreCase(name)
                .filter(d -> Boolean.TRUE.equals(d.getIsVerified()))
                .map(TenantDomain::getTenantId)
                .orElse(null));
    }

    // ---------------------------------------------------------------- helpers

    private TenantDomain markVerified(TenantDomain d) {
        d.setIsVerified(true);
        d.setDateVerified(OffsetDateTime.now());
        d.setVerificationToken(null);
        TenantDomain saved = domains.saveAndFlush(d);
        audit.record("DOMAIN_VERIFIED", "TENANT_DOMAIN", saved.getDomainId(), null, Severity.INFO, null, Map.of("domainName", d.getDomainName()));
        return saved;
    }

    private TenantDomain load(UUID tenantId, UUID domainId) {
        return domains.findByDomainIdAndTenantId(domainId, tenantId).orElseThrow(() -> ApiException.notFound("Domain"));
    }

    private static String normalise(String raw) {
        String n = raw.trim().toLowerCase();
        if (n.startsWith("http://") || n.startsWith("https://")) n = n.substring(n.indexOf("//") + 2);
        if (n.endsWith("/")) n = n.substring(0, n.length() - 1);
        if (!DOMAIN.matcher(n).matches()) throw ApiException.badRequest("INVALID_DOMAIN", "Not a valid domain name: " + raw);
        return n;
    }

    /** Platform admins name the tenant; institution admins (or TENANT_SETTINGS_MANAGE) get their own. */
    private UUID target(UUID tenantId) {
        if (tenantId != null) {
            Access.requirePlatformAdmin();
            tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant"));
            return tenantId;
        }
        Access.requireTenantPermission(Permissions.TENANT_SETTINGS_MANAGE);
        return Access.ctx().tenantId();
    }
}
