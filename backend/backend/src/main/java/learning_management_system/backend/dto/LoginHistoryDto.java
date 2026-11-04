package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.LoginMethod;
import learning_management_system.backend.enums.LoginStatus;

/** One sign-in attempt (table login_history). */
public record LoginHistoryDto(UUID loginId, OffsetDateTime loginTimestamp, LoginStatus loginStatus, LoginMethod loginMethod,
                              String ipAddress, String failureReason, Boolean mfaUsed) {}
