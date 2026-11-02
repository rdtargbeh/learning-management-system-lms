package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.entity.TenantSubscription;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSubscriptionRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.TenantSubscriptionService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantSubscriptionServiceImplementation implements TenantSubscriptionService {
    private final TenantSubscriptionRepository subscriptions;
    private final TenantRepository tenants;
    private final TenantMapper tenantMapper;
    private final EntityRefresher refresher;

    public TenantSubscriptionServiceImplementation(TenantSubscriptionRepository subscriptions, TenantRepository tenants,
                                                   TenantMapper tenantMapper, EntityRefresher refresher) {
        this.subscriptions = subscriptions;
        this.tenants = tenants;
        this.tenantMapper = tenantMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public TenantSubscriptionDto get(UUID tenantId) {
        UUID t = target(tenantId);
        return subscriptions.findByTenantId(t).map(tenantMapper::toDto).orElseThrow(() -> ApiException.notFound("Subscription"));
    }

    @Transactional
    @Override
    public TenantSubscriptionDto upsert(UUID tenantId, UpsertTenantSubscriptionRequest r) {
        Access.requirePlatformAdmin();
        tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant"));
        if (r.dateEnd() != null && r.dateEnd().isBefore(r.dateStart())) throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
        TenantSubscription s = subscriptions.findByTenantId(tenantId).orElseGet(() -> {
            TenantSubscription n = new TenantSubscription();
            n.setTenantId(tenantId);
            return n;
        });
        tenantMapper.replaceEntity(r, s);
        if (s.getIsActive() == null) s.setIsActive(true);
        return tenantMapper.toDto(refresher.refresh(subscriptions.saveAndFlush(s)));
    }

    @Override
    public Optional<TenantSubscription> current(UUID tenantId) {
        LocalDate today = LocalDate.now();
        return subscriptions.findByTenantId(tenantId).filter(s -> Boolean.TRUE.equals(s.getIsActive())
                && !today.isBefore(s.getDateStart()) && (s.getDateEnd() == null || !today.isAfter(s.getDateEnd())));
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
