package learning_management_system.backend.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Issues and verifies HS256 access tokens. Claims carry the tenant and user so every request can set RLS context. */
@Service
public class TokenService {

    public static final String CLAIM_TENANT = "tid";
    public static final String CLAIM_USER_TYPE = "typ";
    public static final String CLAIM_SYSTEM_USER = "sys";
    public static final String CLAIM_PLATFORM_ROLE = "prole";
    public static final String CLAIM_PERMISSIONS = "perms";
    public static final String CLAIM_SUPPORT_ACCESS = "said";
    /** The user_session the token belongs to; a revoked session makes the token useless (see RequestContextFilter). */
    public static final String CLAIM_SESSION = "sid";
    /** Marks a short-lived "password OK, now enter your MFA code" token; it is not accepted by the API. */
    public static final String CLAIM_MFA_PENDING = "mfa";
    private static final long MFA_PENDING_MINUTES = 5;

    private final SecurityProperties props;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public TokenService(SecurityProperties props) {
        this.props = props;
        if (props.jwtSecret() == null || props.jwtSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("LMS_JWT_SECRET must be set to at least 32 bytes");
        }
        SecretKey key = new SecretKeySpec(props.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    public JwtDecoder decoder() { return decoder; }

    public long ttlSeconds() { return props.tokenTtlMinutes() * 60L; }

    public String issue(UUID userId, UUID tenantId, String userType, boolean systemUser, String platformRole,
                        Collection<String> permissions, UUID supportAccessId) {
        return issue(userId, tenantId, userType, systemUser, platformRole, permissions, supportAccessId, null);
    }

    public String issue(UUID userId, UUID tenantId, String userType, boolean systemUser, String platformRole,
                        Collection<String> permissions, UUID supportAccessId, UUID sessionId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.tokenTtlMinutes(), ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim(CLAIM_USER_TYPE, userType)
                .claim(CLAIM_SYSTEM_USER, systemUser)
                .claim(CLAIM_PERMISSIONS, permissions == null ? List.of() : List.copyOf(permissions));
        if (tenantId != null) claims.claim(CLAIM_TENANT, tenantId.toString());
        if (platformRole != null) claims.claim(CLAIM_PLATFORM_ROLE, platformRole);
        if (supportAccessId != null) claims.claim(CLAIM_SUPPORT_ACCESS, supportAccessId.toString());
        if (sessionId != null) claims.claim(CLAIM_SESSION, sessionId.toString());
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    /** The second-step token after a correct password when the user has MFA turned on (5 minutes). */
    public String issueMfaPending(UUID userId, UUID tenantId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().issuer(props.issuer()).issuedAt(now)
                .expiresAt(now.plus(MFA_PENDING_MINUTES, ChronoUnit.MINUTES)).subject(userId.toString()).claim(CLAIM_MFA_PENDING, true);
        if (tenantId != null) claims.claim(CLAIM_TENANT, tenantId.toString());
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }

    /** Checks an MFA-pending token; returns {userId, tenantId (may be null)} or throws JwtException. */
    public UUID[] parseMfaPending(String token) {
        Jwt jwt = decoder.decode(token);
        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean(CLAIM_MFA_PENDING))) throw new BadJwtException("Not an MFA token");
        String tid = jwt.getClaimAsString(CLAIM_TENANT);
        return new UUID[]{UUID.fromString(jwt.getSubject()), tid == null ? null : UUID.fromString(tid)};
    }

    public long mfaPendingSeconds() { return MFA_PENDING_MINUTES * 60; }
}
