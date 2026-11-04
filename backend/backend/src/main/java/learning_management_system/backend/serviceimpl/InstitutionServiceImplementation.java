package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.InstitutionDto;
import learning_management_system.backend.dto.UpdateInstitutionRequest;
import learning_management_system.backend.entity.Institution;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.InstitutionMapper;
import learning_management_system.backend.repository.InstitutionRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.security.SystemTx;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.InstitutionService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InstitutionServiceImplementation implements InstitutionService {
    private final InstitutionRepository institutions;
    private final TenantRepository tenants;
    private final InstitutionMapper mapper;
    private final SystemTx sys;
    private final EntityRefresher refresher;
    private final AuditLogService audit;

    public InstitutionServiceImplementation(InstitutionRepository institutions, TenantRepository tenants, InstitutionMapper mapper,
                                            SystemTx sys, EntityRefresher refresher, AuditLogService audit) {
        this.institutions = institutions;
        this.tenants = tenants;
        this.mapper = mapper;
        this.sys = sys;
        this.refresher = refresher;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public InstitutionDto get(UUID tenantId) {
        UUID t;
        if (tenantId != null) {
            Access.requirePlatformAdmin();
            t = tenantId;
        } else {
            Access.requireTenant();
            t = Access.ctx().tenantId();
        }
        return institutions.findByTenantId(t).map(mapper::toDto).orElseThrow(() -> ApiException.notFound("Institution"));
    }

    @Transactional
    @Override
    public InstitutionDto update(UUID tenantId, UpdateInstitutionRequest r) {
        UUID t;
        if (tenantId != null) {
            Access.requirePlatformAdmin();
            tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant"));
            t = tenantId;
        } else {
            Access.requireTenantPermission(Permissions.TENANT_SETTINGS_MANAGE);
            t = Access.ctx().tenantId();
        }
        Institution i = institutions.findByTenantId(t).orElseGet(() -> {
            if (r.institutionName() == null || r.institutionName().isBlank()) {
                throw ApiException.badRequest("NAME_REQUIRED", "institutionName is required for a new institution record");
            }
            Institution n = new Institution();
            n.setTenantId(t);
            return n;
        });
        if (r.institutionCode() != null) {
            if (!Access.ctx().platformAdmin()) throw ApiException.forbidden("Only platform admins change the institution code");
            String code = r.institutionCode().trim().toUpperCase();
            // codes are unique across all institutions, so the check must see every tenant
            boolean taken = sys.platform(() -> institutions.findByInstitutionCodeIgnoreCase(code)
                    .filter(x -> !x.getTenantId().equals(t)).isPresent());
            if (taken) throw ApiException.conflict("INSTITUTION_CODE_TAKEN", "Institution code already in use");
        }
        mapper.updateEntity(r, i);
        if (r.institutionName() != null) i.setInstitutionName(r.institutionName().trim());
        if (r.institutionCode() != null) i.setInstitutionCode(r.institutionCode().trim().toUpperCase());
        Institution saved = refresher.refresh(institutions.saveAndFlush(i));
        audit.record("INSTITUTION_UPDATED", "INSTITUTION", saved.getInstitutionId(), null, Severity.INFO, null, r);
        return mapper.toDto(saved);
    }
}
