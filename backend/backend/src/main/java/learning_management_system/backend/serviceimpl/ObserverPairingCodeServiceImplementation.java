package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.ObserverPairingCodeDto;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.dto.RedeemPairingCodeRequest;
import learning_management_system.backend.entity.ObserverPairingCode;
import learning_management_system.backend.entity.User;
import learning_management_system.backend.enums.AccountStatus;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.enums.UserType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.ObserverPairingCodeRepository;
import learning_management_system.backend.repository.UserRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.ObserverPairingCodeService;
import learning_management_system.backend.service.ObserverStudentLinkService;
import learning_management_system.backend.utility.EntityRefresher;
import learning_management_system.backend.utility.Tokens;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ObserverPairingCodeServiceImplementation implements ObserverPairingCodeService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";   // no 0/O, 1/I
    private static final int LENGTH = 8;
    private static final int VALID_DAYS = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObserverPairingCodeRepository codes;
    private final UserRepository users;
    private final ObserverStudentLinkService links;
    private final EntityRefresher refresher;
    private final AuditLogService audit;

    public ObserverPairingCodeServiceImplementation(ObserverPairingCodeRepository codes, UserRepository users,
                                                    ObserverStudentLinkService links, EntityRefresher refresher, AuditLogService audit) {
        this.codes = codes;
        this.users = users;
        this.links = links;
        this.refresher = refresher;
        this.audit = audit;
    }

    @Transactional
    @Override
    public ObserverPairingCodeDto create(UUID studentUserId) {
        requireStudentOrManager(studentUserId);
        OffsetDateTime now = OffsetDateTime.now();
        codes.expireOpen(studentUserId, now);
        String code = newCode();
        ObserverPairingCode c = new ObserverPairingCode();
        c.setStudentUserId(studentUserId);
        c.setCodeHash(Tokens.sha256(code));
        c.setDateExpires(now.plusDays(VALID_DAYS));
        c = refresher.refresh(codes.saveAndFlush(c));
        return toDto(c, code);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ObserverPairingCodeDto> list(UUID studentUserId) {
        requireStudentOrManager(studentUserId);
        return codes.findByStudentUserIdOrderByDateCreatedDesc(studentUserId).stream().map(c -> toDto(c, null)).toList();
    }

    @Transactional
    @Override
    public ObserverStudentLinkDto redeem(RedeemPairingCodeRequest r) {
        Access.requireTenant();
        UUID me = Access.ctx().userId();
        User observer = users.findById(me).orElseThrow(() -> ApiException.unauthorized("Sign in first"));
        if (observer.getUserType() != UserType.OBSERVER) {
            throw ApiException.forbidden("Only observer accounts can pair with a student");
        }
        String normalised = r.code().trim().toUpperCase(Locale.ROOT).replace("-", "").replace(" ", "");
        ObserverPairingCode c = codes.findFirstByCodeHash(Tokens.sha256(normalised)).orElse(null);
        if (c == null || c.getUsedBy() != null || !c.getDateExpires().isAfter(OffsetDateTime.now())) {
            throw ApiException.badRequest("INVALID_CODE", "The pairing code is invalid, used or expired");
        }
        User student = users.findById(c.getStudentUserId()).filter(u -> u.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> ApiException.badRequest("INVALID_CODE", "The pairing code is invalid, used or expired"));
        ObserverStudentLinkDto link = links.pair(me, student.getUserId(), r.relationship());
        c.setUsedBy(me);
        c.setDateUsed(OffsetDateTime.now());
        codes.saveAndFlush(c);
        audit.record("OBSERVER_PAIRED", "OBSERVER_STUDENT_LINK", link.observerLinkId(), student.getUserId(), Severity.INFO, null, null);
        return link;
    }

    private void requireStudentOrManager(UUID studentUserId) {
        Access.requireTenant();
        User s = users.findById(studentUserId).orElseThrow(() -> ApiException.notFound("Student"));
        if (s.getUserType() != UserType.STUDENT) throw ApiException.badRequest("NOT_A_STUDENT", "Only students have pairing codes");
        if (!Access.isSelf(studentUserId)) Access.requireTenantPermission(Permissions.OBSERVER_MANAGE);
    }

    private static String newCode() {
        StringBuilder b = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) b.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return b.toString();
    }

    private static ObserverPairingCodeDto toDto(ObserverPairingCode c, String code) {
        return new ObserverPairingCodeDto(c.getPairingCodeId(), c.getStudentUserId(), code, c.getDateExpires(), c.getUsedBy() != null,
                c.getUsedBy(), c.getDateUsed(), c.getDateCreated());
    }
}
