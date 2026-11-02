package learning_management_system.backend.service;

import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.enums.LoginStatus;

import java.net.InetAddress;
import java.util.UUID;

/** login_history: every sign-in attempt, successful or not. */
public interface LoginHistoryService {

    /** Records an attempt (trusted: runs before anyone is signed in). userId is null for an unknown login. */
    void record(UUID userId, UUID tenantId, String login, InetAddress ip, LoginStatus status, String failureReason, boolean mfaUsed);

    /** A user's attempts, newest first: their own, or anyone's with USER_READ. */
    PageResponse<LoginHistoryDto> list(UUID userId, int page, int size);
}
