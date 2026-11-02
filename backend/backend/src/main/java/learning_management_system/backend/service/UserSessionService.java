package learning_management_system.backend.service;

import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.SessionDto;
import learning_management_system.backend.entity.User;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

/**
 * Sign-in sessions (table user_session). Each sign-in creates a session with a short-lived access token and a
 * long-lived refresh token; refreshing rotates the refresh token. Signing out, changing the password, being
 * deactivated or locked ends sessions, and an ended session's access tokens stop working at once.
 */
public interface UserSessionService {

    /** Starts a session for a user whose credentials (and MFA) were checked; runs in the caller's trusted context. */
    LoginResponse start(User user, UUID tenantId, InetAddress ip, String userAgent, boolean mfaSetupRequired);

    /** New access + refresh token for a valid refresh token (no sign-in needed). */
    LoginResponse refresh(String refreshToken, InetAddress ip, String userAgent);

    /** Ends the caller's current session. */
    void logout();

    /** The caller's active sessions. */
    List<SessionDto> mine();

    /** Ends one of the caller's sessions (or, with USER_MANAGE, someone else's in the institution). */
    void revoke(UUID sessionId);

    /** Ends every session of a user (internal). */
    void revokeAll(UUID userId);

    /** For the request filter: is this session still active? Cached briefly. */
    boolean isActive(UUID sessionId);
}
