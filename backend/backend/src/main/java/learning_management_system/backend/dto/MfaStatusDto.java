package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import learning_management_system.backend.enums.MfaType;

/** The caller's MFA: whether it is on, which kind, how many backup codes are left, and whether it is required. */
public record MfaStatusDto(boolean enabled, MfaType mfaType, int backupCodesRemaining, OffsetDateTime lastMfaAt, boolean required) {}
