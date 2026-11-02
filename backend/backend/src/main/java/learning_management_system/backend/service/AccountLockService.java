package learning_management_system.backend.service;

import learning_management_system.backend.dto.AccountLockDto;
import learning_management_system.backend.dto.LockAccountRequest;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Account locks (table account_lock): automatic ones after too many wrong passwords, and administrator locks
 * (indefinite or until a time). A locked account cannot sign in and its sessions end.
 */
public interface AccountLockService {

    AccountLockDto get(UUID userId);

    AccountLockDto lock(UUID userId, LockAccountRequest r);

    AccountLockDto unlock(UUID userId);

    /** Sign-in (trusted context): note an automatic lock after failed passwords. */
    void recordAutomaticLock(UUID userId, int failedAttempts, InetAddress ip, OffsetDateTime until);

    /** Sign-in (trusted context): an administrator lock whose time has passed is lifted; returns true if still locked. */
    boolean stillLocked(UUID userId);

    /** After a password reset: lift an automatic lock. */
    void clearAutomaticLock(UUID userId);
}
