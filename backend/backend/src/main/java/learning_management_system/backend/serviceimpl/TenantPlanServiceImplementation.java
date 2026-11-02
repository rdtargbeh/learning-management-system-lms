package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.TenantSubscriptionDto;
import learning_management_system.backend.dto.TenantUserLimitDto;
import learning_management_system.backend.dto.UpdateTenantUserLimitRequest;
import learning_management_system.backend.dto.UpsertTenantSubscriptionRequest;
import learning_management_system.backend.entity.TenantSubscription;
import learning_management_system.backend.entity.TenantUserLimit;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.TenantMapper;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.TenantSubscriptionRepository;
import learning_management_system.backend.repository.TenantUserLimitRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.TenantPlanService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class TenantPlanServiceImplementation implements TenantPlanService {
    private static final Set<AccountStatus> NOT_COUNTED = EnumSet.of(AccountStatus.DEACTIVATED, AccountStatus.DELETED);
    private static final Set<UserType> STUDENTS = EnumSet.of(UserType.STUDENT);
    private static final Set<UserType> INSTRUCTORS = EnumSet.of(UserType.INSTRUCTOR, UserType.TEACHING_ASSISTANT);
    private static final Set<UserType> STAFF = EnumSet.of(UserType.STAFF, UserType.ADVISOR);
    private static final Set<UserType> ADMINS = EnumSet.of(UserType.TENANT_ADMIN);

    private final TenantSubscriptionRepository subscriptions;
    private final TenantUserLimitRepository limits;
    private final TenantRepository tenants;
    private final UserRepository users;
    private final TenantMapper tenantMapper;
    private final EntityRefresher refresher;

    public TenantPlanServiceImplementation(TenantSubscriptionRepository subscriptions, TenantUserLimitRepository limits,
                                           TenantRepository tenants, UserRepository users, TenantMapper tenantMapper,
                                           EntityRefresher refresher) {
        this.subscriptions = subscriptions;
        this.limits = limits;
        this.tenants = tenants;
        this.users = users;
        this.tenantMapper = tenantMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public TenantSubscriptionDto subscription(UUID tenantId) {
        UUID t = target(tenantId);
        return subscriptions.findByTenantId(t).map(tenantMapper::toDto).orElseThrow(() -> ApiException.notFound("Subscription"));
    }

    @Transactional
    @Override
    public TenantSubscriptionDto upsertSubscription(UUID tenantId, UpsertTenantSubscriptionRequest r) {
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

    @Transactional
    @Override
    public TenantUserLimitDto userLimits(UUID tenantId) {
        UUID t = target(tenantId);
        refreshCounts(t);
        return tenantMapper.toDto(limits.findByTenantId(t).orElseThrow());
    }

    @Transactional
    @Override
    public TenantUserLimitDto updateUserLimits(UUID tenantId, UpdateTenantUserLimitRequest r) {
        Access.requirePlatformAdmin();
        tenants.findById(tenantId).orElseThrow(() -> ApiException.notFound("Tenant"));
        TenantUserLimit l = row(tenantId);
        tenantMapper.replaceEntity(r, l);
        limits.saveAndFlush(l);
        refreshCounts(tenantId);
        return tenantMapper.toDto(limits.findByTenantId(tenantId).orElseThrow());
    }

    @Override
    public void requireRoom(UUID tenantId, UserType type) {
        TenantUserLimit l = limits.lockByTenantId(tenantId).orElse(null);
        TenantSubscription sub = subscriptions.findByTenantId(tenantId).filter(this::current).orElse(null);
        Integer max;
        Set<UserType> group;
        String label;
        switch (type) {
            case STUDENT -> { group = STUDENTS; label = "students"; max = first(l == null ? null : l.getMaxStudents(), sub == null ? null : sub.getMaxStudents()); }
            case INSTRUCTOR, TEACHING_ASSISTANT -> { group = INSTRUCTORS; label = "instructors"; max = first(l == null ? null : l.getMaxInstructors(), sub == null ? null : sub.getMaxInstructors()); }
            case STAFF, ADVISOR -> { group = STAFF; label = "staff"; max = l == null ? null : l.getMaxStaff(); }
            case TENANT_ADMIN -> { group = ADMINS; label = "admins"; max = l == null ? null : l.getMaxAdmins(); }
            default -> { return; }                      // observers (and platform users) are not limited
        }
        if (max != null && users.countByTenantIdAndUserTypeInAndAccountStatusNotIn(tenantId, group, NOT_COUNTED) >= max) {
            throw ApiException.conflict("USER_LIMIT_REACHED", "The institution's limit of " + max + " " + label + " is reached");
        }
    }

    @Override
    public void refreshCounts(UUID tenantId) {
        TenantUserLimit l = row(tenantId);
        l.setCurrentStudents((int) users.countByTenantIdAndUserTypeInAndAccountStatusNotIn(tenantId, STUDENTS, NOT_COUNTED));
        l.setCurrentInstructors((int) users.countByTenantIdAndUserTypeInAndAccountStatusNotIn(tenantId, INSTRUCTORS, NOT_COUNTED));
        l.setCurrentStaff((int) users.countByTenantIdAndUserTypeInAndAccountStatusNotIn(tenantId, STAFF, NOT_COUNTED));
        l.setCurrentAdmins((int) users.countByTenantIdAndUserTypeInAndAccountStatusNotIn(tenantId, ADMINS, NOT_COUNTED));
        limits.saveAndFlush(l);
    }

    // ---------------------------------------------------------------- helpers

    private TenantUserLimit row(UUID tenantId) {
        return limits.findByTenantId(tenantId).orElseGet(() -> {
            TenantUserLimit n = new TenantUserLimit();
            n.setTenantId(tenantId);
            return refresher.refresh(limits.saveAndFlush(n));
        });
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

    private boolean current(TenantSubscription s) {
        LocalDate today = LocalDate.now();
        return Boolean.TRUE.equals(s.getIsActive()) && !today.isBefore(s.getDateStart()) && (s.getDateEnd() == null || !today.isAfter(s.getDateEnd()));
    }

    private static Integer first(Integer a, Integer b) {
        return a != null ? a : b;
    }
}
