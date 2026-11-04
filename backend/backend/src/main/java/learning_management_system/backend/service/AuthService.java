package learning_management_system.backend.service;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginHistoryDto;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.MfaVerifyRequest;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.UserDetailDto;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Sign-in for platform staff (no tenantCode) and institution users (tenantCode required): password, then (when the user
 * has it on) an MFA code, then a session. Five wrong passwords lock the account for 15 minutes. The signed-in user's own
 * account, password and sign-in history, and tokens for support sessions.
 */
public interface AuthService {

    /** login = username, email, or one of the user's extra sign-in names. */
    LoginResponse login(LoginRequest r, InetAddress ip, String userAgent);

    /** Second step for users with MFA: the mfaToken from login and a code. */
    LoginResponse verifyMfa(MfaVerifyRequest r, InetAddress ip, String userAgent);

    /** The signed-in user with all of their profiles. */
    UserDetailDto me();

    /** Changes the caller's password and ends their other sessions. */
    void changeOwnPassword(ChangePasswordRequest r);

    /** Builds a tenant-scoped token for an active support-access session (used by PlatformSupportAccessService). */
    String supportToken(UUID platformUserId, UUID tenantId, UUID supportAccessId);

    long ttlSeconds();
}
