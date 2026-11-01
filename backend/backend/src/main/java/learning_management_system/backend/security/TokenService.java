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
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.tokenTtlMinutes(), ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim(CLAIM_USER_TYPE, userType)
                .claim(CLAIM_SYSTEM_USER, systemUser)
                .claim(CLAIM_PERMISSIONS, permissions == null ? java.util.List.of() : java.util.List.copyOf(permissions));
        if (tenantId != null) claims.claim(CLAIM_TENANT, tenantId.toString());
        if (platformRole != null) claims.claim(CLAIM_PLATFORM_ROLE, platformRole);
        if (supportAccessId != null) claims.claim(CLAIM_SUPPORT_ACCESS, supportAccessId.toString());
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
