package learning_management_system.backend.dto;

import java.util.List;
import java.util.UUID;

/**
 * Result of signing in. Normally an access token (short-lived) plus a refresh token (long-lived, single use). When
 * the user has MFA on, mfaRequired = true and only mfaToken is filled: send it with the code to /auth/mfa/verify.
 * mfaSetupRequired = true means the account must set up MFA.
 */
public record LoginResponse(String accessToken, String tokenType, Long expiresIn, String refreshToken, Long refreshExpiresIn,
                            UUID sessionId, UserSummaryDto user, List<String> permissions, String platformRole,
                            UUID supportAccessId, boolean mfaRequired, String mfaToken, boolean mfaSetupRequired) {}
