package learning_management_system.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private final TokenService tokens = new TokenService(new SecurityProperties("test-secret-0123456789-abcdefghijklmnop", 30, "lms-test"));

    @Test
    void tenantTokenCarriesTenantUserAndPermissions() {
        UUID user = UUID.randomUUID(), tenant = UUID.randomUUID();
        Jwt jwt = tokens.decoder().decode(tokens.issue(user, tenant, "STUDENT", false, null, List.of("USER_READ"), null));
        assertThat(jwt.getSubject()).isEqualTo(user.toString());
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_TENANT)).isEqualTo(tenant.toString());
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_USER_TYPE)).isEqualTo("STUDENT");
        assertThat(jwt.getClaimAsStringList(TokenService.CLAIM_PERMISSIONS)).containsExactly("USER_READ");
        assertThat(jwt.getClaimAsBoolean(TokenService.CLAIM_SYSTEM_USER)).isFalse();
    }

    @Test
    void platformTokenHasNoTenant() {
        Jwt jwt = tokens.decoder().decode(tokens.issue(UUID.randomUUID(), null, "PLATFORM", true, "SUPER_ADMIN", List.of(), null));
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_TENANT)).isNull();
        assertThat(jwt.getClaimAsString(TokenService.CLAIM_PLATFORM_ROLE)).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void tokenFromAnotherSecretIsRejected() {
        TokenService other = new TokenService(new SecurityProperties("another-secret-0123456789-abcdefghijk", 30, "lms-test"));
        String foreign = other.issue(UUID.randomUUID(), null, "PLATFORM", true, "SUPER_ADMIN", List.of(), null);
        assertThatThrownBy(() -> tokens.decoder().decode(foreign)).isInstanceOf(JwtException.class);
    }

    @Test
    void shortSecretIsRefused() {
        assertThatThrownBy(() -> new TokenService(new SecurityProperties("too-short", 30, "x"))).isInstanceOf(IllegalStateException.class);
    }
}
