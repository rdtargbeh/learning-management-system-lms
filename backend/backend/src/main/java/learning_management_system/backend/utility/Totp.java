package learning_management_system.backend.utility;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Time-based one-time passwords (RFC 6238: HMAC-SHA1, 30-second steps, 6 digits), compatible with Google Authenticator,
 * Microsoft Authenticator, 1Password and similar apps. Secrets are exchanged in Base32 (RFC 4648).
 */
public final class Totp {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int STEP_SECONDS = 30;
    private static final int DIGITS = 6;

    private Totp() {}

    /** A new 160-bit secret in Base32. */
    public static String newSecret() {
        byte[] b = new byte[20];
        RANDOM.nextBytes(b);
        return base32(b);
    }

    /** The code for a moment in time. */
    public static String code(String base32Secret, Instant at) {
        return code(base32Decode(base32Secret), at.getEpochSecond() / STEP_SECONDS);
    }

    /** True when the code matches now, or one step before/after (clock drift). */
    public static boolean verify(String base32Secret, String code, Instant now) {
        if (code == null || !code.matches("\\d{6}")) return false;
        byte[] key = base32Decode(base32Secret);
        long step = now.getEpochSecond() / STEP_SECONDS;
        for (long s = step - 1; s <= step + 1; s++) {
            if (java.security.MessageDigest.isEqual(code(key, s).getBytes(), code.getBytes())) return true;
        }
        return false;
    }

    /** otpauth:// link for a QR code. */
    public static String otpauthUri(String issuer, String account, String base32Secret) {
        String i = enc(issuer), a = enc(account);
        return "otpauth://totp/" + i + ":" + a + "?secret=" + base32Secret + "&issuer=" + i + "&algorithm=SHA1&digits=6&period=30";
    }

    private static String code(byte[] key, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int o = h[h.length - 1] & 0x0f;
            int bin = ((h[o] & 0x7f) << 24) | ((h[o + 1] & 0xff) << 16) | ((h[o + 2] & 0xff) << 8) | (h[o + 3] & 0xff);
            return String.format("%0" + DIGITS + "d", bin % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    static String base32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bits = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                sb.append(ALPHABET.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) sb.append(ALPHABET.charAt((buffer << (5 - bits)) & 31));
        return sb.toString();
    }

    static byte[] base32Decode(String s) {
        String in = s.replace("=", "").replace(" ", "").toUpperCase();
        ByteBuffer out = ByteBuffer.allocate(in.length() * 5 / 8);
        int buffer = 0, bits = 0;
        for (char c : in.toCharArray()) {
            int v = ALPHABET.indexOf(c);
            if (v < 0) throw new IllegalArgumentException("Not Base32");
            buffer = (buffer << 5) | v;
            bits += 5;
            if (bits >= 8) {
                out.put((byte) ((buffer >> (bits - 8)) & 0xff));
                bits -= 8;
            }
        }
        byte[] r = new byte[out.position()];
        out.flip();
        out.get(r);
        return r;
    }

    private static String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
