package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AccountLockDto;
import learning_management_system.backend.dto.LockAccountRequest;
import learning_management_system.backend.entity.AccountLock;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.IdentityMapper;
import learning_management_system.backend.repository.AccountLockRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AccountLockService;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.UserSessionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountLockServiceImplementation implements AccountLockService {
    static final String AUTOMATIC = "Too many failed sign-ins";

    private final AccountLockRepository locks;
    private final UserRepository users;
    private final UserSessionService sessions;
    private final AuditLogService audit;
    private final IdentityMapper identityMapper;

    public AccountLockServiceImplementation(AccountLockRepository locks, UserRepository users, UserSessionService sessions,
                                            AuditLogService audit, IdentityMapper identityMapper) {
        this.locks = locks;
        this.users = users;
        this.sessions = sessions;
        this.audit = audit;
        this.identityMapper = identityMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public AccountLockDto get(UUID userId) {
        target(userId, Permissions.USER_READ);
        return locks.findByUserId(userId).map(identityMapper::toDto)
                .orElse(new AccountLockDto(false, null, 0, null, null, null, null, null));
    }

    @Transactional
    @Override
    public AccountLockDto lock(UUID userId, LockAccountRequest r) {
        User u = target(userId, Permissions.USER_MANAGE);
        if (Access.isSelf(userId)) throw ApiException.badRequest("SELF_LOCK", "You cannot lock your own account");
        OffsetDateTime now = OffsetDateTime.now();
        if (r.lockedUntil() != null && !r.lockedUntil().isAfter(now)) throw ApiException.badRequest("INVALID_DATE", "lockedUntil must be in the future");
        AccountLock l = row(u);
        l.setIsLocked(true);
        l.setLockReason(r.reason().trim());
        l.setDateLocked(now);
        l.setLockedUntil(r.lockedUntil());
        l.setLockedBy(Access.ctx().userId());
        l.setDateUnlocked(null);
        l.setUnlockedBy(null);
        locks.saveAndFlush(l);
        u.setAccountStatus(AccountStatus.LOCKED);
        users.saveAndFlush(u);
        sessions.revokeAll(userId);
        audit.record("ACCOUNT_LOCKED", "USER", userId, userId, Severity.WARNING, null, Map.of("reason", l.getLockReason()));
        return identityMapper.toDto(l);
    }

    @Transactional
    @Override
    public AccountLockDto unlock(UUID userId) {
        User u = target(userId, Permissions.USER_MANAGE);
        AccountLock l = row(u);
        l.setIsLocked(false);
        l.setDateUnlocked(OffsetDateTime.now());
        l.setUnlockedBy(Access.ctx().userId());
        l.setFailedAttempts(0);
        locks.saveAndFlush(l);
        if (u.getAccountStatus() == AccountStatus.LOCKED) u.setAccountStatus(AccountStatus.ACTIVE);
        u.setAccountLockedUntil(null);
        u.setFailedLoginAttempts(0);
        users.saveAndFlush(u);
        audit.record("ACCOUNT_UNLOCKED", "USER", userId, userId, Severity.INFO, null, null);
        return identityMapper.toDto(l);
    }

    @Override
    public void recordAutomaticLock(UUID userId, int failedAttempts, InetAddress ip, OffsetDateTime until) {
        users.findById(userId).ifPresent(u -> {
            AccountLock l = row(u);
            if (Boolean.TRUE.equals(l.getIsLocked()) && !AUTOMATIC.equals(l.getLockReason())) return;   // keep an admin lock
            l.setIsLocked(true);
            l.setLockReason(AUTOMATIC);
            l.setFailedAttempts(failedAttempts);
            l.setLastFailedLoginIp(ip);
            l.setDateLocked(OffsetDateTime.now());
            l.setLockedUntil(until);
            locks.saveAndFlush(l);
        });
    }

    @Override
    public boolean stillLocked(UUID userId) {
        AccountLock l = locks.findByUserId(userId).orElse(null);
        User u = users.findById(userId).orElse(null);
        if (u == null || u.getAccountStatus() != AccountStatus.LOCKED) return false;
        if (l != null && l.getLockedUntil() != null && l.getLockedUntil().isBefore(OffsetDateTime.now())) {
            l.setIsLocked(false);
            l.setDateUnlocked(OffsetDateTime.now());
            locks.saveAndFlush(l);
            u.setAccountStatus(AccountStatus.ACTIVE);
            users.saveAndFlush(u);
            return false;
        }
        return true;
    }

    @Override
    public void clearAutomaticLock(UUID userId) {
        locks.findByUserId(userId).filter(l -> Boolean.TRUE.equals(l.getIsLocked()) && AUTOMATIC.equals(l.getLockReason())).ifPresent(l -> {
            l.setIsLocked(false);
            l.setDateUnlocked(OffsetDateTime.now());
            l.setFailedAttempts(0);
            locks.saveAndFlush(l);
        });
    }

    private AccountLock row(User u) {
        return locks.findByUserId(u.getUserId()).orElseGet(() -> {
            AccountLock n = new AccountLock();
            n.setUserId(u.getUserId());
            n.setTenantId(u.getTenantId());
            n.setIsLocked(false);
            n.setFailedAttempts(0);
            return n;
        });
    }

    private User target(UUID userId, String permission) {
        User u = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (Boolean.TRUE.equals(u.getIsSystemUser())) Access.requirePlatformAdmin();
        else if (!Access.isSelf(userId) || Permissions.USER_MANAGE.equals(permission)) Access.requireTenantPermission(permission);
        return u;
    }
}
