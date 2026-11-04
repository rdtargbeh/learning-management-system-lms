package learning_management_system.backend.utility;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TotpTest {

    /** RFC 6238 appendix B test vector (SHA-1, secret "12345678901234567890"), last 6 digits. */
    @Test
    void matchesRfc6238Vectors() {
        String secret = Totp.base32("12345678901234567890".getBytes(StandardCharsets.US_ASCII));
        assertThat(Totp.code(secret, Instant.ofEpochSecond(59))).isEqualTo("287082");
        assertThat(Totp.code(secret, Instant.ofEpochSecond(1111111109))).isEqualTo("081804");
        assertThat(Totp.code(secret, Instant.ofEpochSecond(2000000000))).isEqualTo("279037");
    }

    @Test
    void acceptsOneStepOfClockDriftOnly() {
        String secret = Totp.newSecret();
        Instant now = Instant.ofEpochSecond(1_800_000_000L);
        assertThat(Totp.verify(secret, Totp.code(secret, now.minusSeconds(30)), now)).isTrue();
        assertThat(Totp.verify(secret, Totp.code(secret, now.plusSeconds(30)), now)).isTrue();
        assertThat(Totp.verify(secret, Totp.code(secret, now.minusSeconds(90)), now)).isFalse();
        assertThat(Totp.verify(secret, "12ab56", now)).isFalse();
    }

    @Test
    void base32RoundTrip() {
        byte[] data = {0, 1, 2, -1, 127, -128, 42};
        assertThat(Totp.base32Decode(Totp.base32(data))).isEqualTo(data);
    }
}
