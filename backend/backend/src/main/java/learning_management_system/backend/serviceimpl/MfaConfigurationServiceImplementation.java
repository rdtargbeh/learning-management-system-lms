package learning_management_system.backend.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import learning_management_system.backend.dto.BackupCodesDto;
import learning_management_system.backend.dto.DisableMfaRequest;
import learning_management_system.backend.dto.MfaStatusDto;
import learning_management_system.backend.dto.TotpSetupDto;
import learning_management_system.backend.entity.MfaConfiguration;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.MfaType;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.MfaConfigurationRepository;
import learning_management_system.backend.repository.TenantRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.MfaConfigurationService;
import learning_management_system.backend.utility.SecretCipher;
import learning_management_system.backend.utility.Tokens;
import learning_management_system.backend.utility.Totp;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MfaConfigurationServiceImplementation implements MfaConfigurationService {
    private static final int BACKUP_CODES = 10;
    private static final int MAX_FAILED = 5;
    private static final int LOCK_MINUTES = 15;
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MfaConfigurationRepository configs;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final SecretCipher cipher;
    private final PasswordEncoder encoder;
    private final AuditLogService audit;
    private final ObjectMapper json;

    public MfaConfigurationServiceImplementation(MfaConfigurationRepository configs, UserRepository users, TenantRepository tenants,
                                    SecretCipher cipher, PasswordEncoder encoder, AuditLogService audit, ObjectMapper json) {
        this.configs = configs;
        this.users = users;
        this.tenants = tenants;
        this.cipher = cipher;
        this.encoder = encoder;
        this.audit = audit;
        this.json = json;
    }

    @Transactional(readOnly = true)
    @Override
    public MfaStatusDto status() {
        User u = me();
        MfaConfiguration c = configs.findByUserId(u.getUserId()).orElse(null);
        boolean on = c != null && Boolean.TRUE.equals(c.getMfaEnabled());
        return new MfaStatusDto(on, on ? c.getMfaType() : null, on && c.getBackupCodes() != null ? c.getBackupCodes().size() : 0,
                c == null ? null : c.getLastMfaAt(), Boolean.TRUE.equals(u.getMfaRequired()));
    }

    @Transactional
    @Override
    public TotpSetupDto setupTotp() {
        User u = me();
        MfaConfiguration c = configs.findByUserId(u.getUserId()).orElseGet(() -> {
            MfaConfiguration n = new MfaConfiguration();
            n.setUserId(u.getUserId());
            n.setTenantId(u.getTenantId());
            return n;
        });
        if (Boolean.TRUE.equals(c.getMfaEnabled())) throw ApiException.conflict("MFA_ALREADY_ON", "MFA is already on; turn it off first");
        String secret = Totp.newSecret();
        c.setMfaEnabled(false);
        c.setMfaType(MfaType.TOTP);
        c.setTotpSecretEncrypted(cipher.encrypt(secret));
        c.setFailedMfaAttempts(0);
        configs.saveAndFlush(c);
        String issuer = u.getTenantId() == null ? "LMS" : tenants.findById(u.getTenantId()).map(t -> t.getTenantName()).orElse("LMS");
        return new TotpSetupDto(secret, Totp.otpauthUri(issuer, u.getUsername(), secret));
    }

    @Transactional
    @Override
    public BackupCodesDto enableTotp(String code) {
        User u = me();
        MfaConfiguration c = configs.findByUserId(u.getUserId())
                .filter(x -> x.getTotpSecretEncrypted() != null)
                .orElseThrow(() -> ApiException.conflict("MFA_NOT_SET_UP", "Start the setup first"));
        if (Boolean.TRUE.equals(c.getMfaEnabled())) throw ApiException.conflict("MFA_ALREADY_ON", "MFA is already on");
        if (!Totp.verify(cipher.decrypt(c.getTotpSecretEncrypted()), code, Instant.now())) {
            throw ApiException.badRequest("WRONG_CODE", "The code does not match; check the time on your phone");
        }
        c.setMfaEnabled(true);
        c.setLastMfaAt(OffsetDateTime.now());
        List<String> codes = newBackupCodes(c);
        configs.saveAndFlush(c);
        audit.record("MFA_ENABLED", "USER", u.getUserId(), u.getUserId(), Severity.INFO, null, null);
        return new BackupCodesDto(codes);
    }

    @Transactional
    @Override
    public BackupCodesDto regenerateBackupCodes(String code) {
        User u = me();
        MfaConfiguration c = enabledConfig(u.getUserId());
        if (!Totp.verify(cipher.decrypt(c.getTotpSecretEncrypted()), code, Instant.now())) throw ApiException.badRequest("WRONG_CODE", "The code does not match");
        List<String> codes = newBackupCodes(c);
        configs.saveAndFlush(c);
        audit.record("MFA_BACKUP_CODES_RENEWED", "USER", u.getUserId(), u.getUserId(), Severity.INFO, null, null);
        return new BackupCodesDto(codes);
    }

    @Transactional
    @Override
    public void disable(DisableMfaRequest r) {
        User u = me();
        if (Boolean.TRUE.equals(u.getMfaRequired())) throw ApiException.conflict("MFA_REQUIRED", "This account must keep MFA on");
        MfaConfiguration c = enabledConfig(u.getUserId());
        if (u.getPasswordHash() == null || !encoder.matches(r.password(), u.getPasswordHash())) {
            throw ApiException.badRequest("WRONG_PASSWORD", "The password is incorrect");
        }
        if (!check(c, r.code())) throw ApiException.badRequest("WRONG_CODE", "The code does not match");
        configs.delete(c);
        configs.flush();
        audit.record("MFA_DISABLED", "USER", u.getUserId(), u.getUserId(), Severity.WARNING, null, null);
    }

    @Transactional
    @Override
    public void adminReset(UUID userId) {
        User target = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        if (Boolean.TRUE.equals(target.getIsSystemUser())) Access.requirePlatformAdmin();
        else Access.requireTenantPermission(Permissions.USER_MANAGE);
        configs.findByUserId(userId).ifPresent(c -> { configs.delete(c); configs.flush(); });
        audit.record("MFA_RESET_BY_ADMIN", "USER", userId, userId, Severity.WARNING, null, null);
    }

    @Override
    public boolean isEnabled(UUID userId) {
        return configs.findByUserId(userId).map(c -> Boolean.TRUE.equals(c.getMfaEnabled())).orElse(false);
    }

    @Override
    public boolean verifyLoginCode(UUID userId, String code) {
        MfaConfiguration c = configs.findByUserId(userId).filter(x -> Boolean.TRUE.equals(x.getMfaEnabled())).orElse(null);
        if (c == null) return false;
        boolean ok = check(c, code);
        if (ok) {
            c.setFailedMfaAttempts(0);
            c.setLastMfaAt(OffsetDateTime.now());
        } else {
            int failed = (c.getFailedMfaAttempts() == null ? 0 : c.getFailedMfaAttempts()) + 1;
            c.setFailedMfaAttempts(failed);
            if (failed >= MAX_FAILED) {
                c.setFailedMfaAttempts(0);
                users.findById(userId).ifPresent(u -> {
                    u.setAccountLockedUntil(OffsetDateTime.now().plusMinutes(LOCK_MINUTES));
                    users.save(u);
                });
            }
        }
        configs.saveAndFlush(c);
        return ok;
    }

    // ---------------------------------------------------------------- helpers

    /** A 6-digit authenticator code, or a backup code (used up when it matches). */
    private boolean check(MfaConfiguration c, String code) {
        String trimmed = code == null ? "" : code.trim();
        if (trimmed.matches("\\d{6}")) return Totp.verify(cipher.decrypt(c.getTotpSecretEncrypted()), trimmed, Instant.now());
        String hash = Tokens.sha256(trimmed.toUpperCase(Locale.ROOT).replace("-", ""));
        if (c.getBackupCodes() == null || !c.getBackupCodes().isArray()) return false;
        ArrayNode left = json.createArrayNode();
        boolean used = false;
        for (var n : c.getBackupCodes()) {
            if (!used && Tokens.sameHash(n.asText(), hash)) used = true;
            else left.add(n.asText());
        }
        if (used) c.setBackupCodes(left);
        return used;
    }

    private List<String> newBackupCodes(MfaConfiguration c) {
        List<String> codes = new ArrayList<>();
        ArrayNode hashes = json.createArrayNode();
        for (int i = 0; i < BACKUP_CODES; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++) sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            codes.add(sb.substring(0, 4) + "-" + sb.substring(4));
            hashes.add(Tokens.sha256(sb.toString()));
        }
        c.setBackupCodes(hashes);
        return codes;
    }

    private MfaConfiguration enabledConfig(UUID userId) {
        return configs.findByUserId(userId).filter(x -> Boolean.TRUE.equals(x.getMfaEnabled()))
                .orElseThrow(() -> ApiException.conflict("MFA_OFF", "MFA is not on"));
    }

    private User me() {
        UUID id = Access.ctx().userId();
        if (id == null) throw ApiException.unauthorized("Sign in first");
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User"));
    }
}
