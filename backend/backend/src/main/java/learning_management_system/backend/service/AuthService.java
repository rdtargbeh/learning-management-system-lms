package learning_management_system.backend.service;

import learning_management_system.backend.dto.ChangePasswordRequest;
import learning_management_system.backend.dto.LoginRequest;
import learning_management_system.backend.dto.LoginResponse;
import learning_management_system.backend.dto.UserDetailDto;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Password sign-in for platform staff (no tenantCode) and institution users (tenantCode required), the signed-in
 * user's own account, and tokens for support sessions.
 */
public interface AuthService {

    LoginResponse login(LoginRequest r, InetAddress ip);

    /** The signed-in user with all of their profiles. */
    UserDetailDto me();

    void changeOwnPassword(ChangePasswordRequest r);

    /** Builds a tenant-scoped token for an active support-access session (used by PlatformService). */
    String supportToken(UUID platformUserId, UUID tenantId, UUID supportAccessId);

    long ttlSeconds();
}
