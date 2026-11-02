package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Lock state of an account (table account_lock). */
public record AccountLockDto(Boolean isLocked, String lockReason, Integer failedAttempts, OffsetDateTime dateLocked,
                             OffsetDateTime lockedUntil, OffsetDateTime dateUnlocked, UUID lockedBy, UUID unlockedBy) {}
