package learning_management_system.backend.service;

import learning_management_system.backend.dto.ObserverPairingCodeDto;
import learning_management_system.backend.dto.ObserverStudentLinkDto;
import learning_management_system.backend.dto.RedeemPairingCodeRequest;

import java.util.List;
import java.util.UUID;

/**
 * observer_pairing_code: as in Canvas, a student (or an administrator with OBSERVER_MANAGE) creates a short code and
 * gives it to a parent or mentor, who enters it in their observer account to follow the student. Codes are single
 * use, expire after 7 days, are stored only as a hash, and a new code replaces the student's unused ones.
 */
public interface ObserverPairingCodeService {

    /** Returns the code once. */
    ObserverPairingCodeDto create(UUID studentUserId);

    /** The student's codes without the codes themselves (the student or OBSERVER_MANAGE). */
    List<ObserverPairingCodeDto> list(UUID studentUserId);

    /** Observers redeem a code; creates (or re-activates) the observer link. */
    ObserverStudentLinkDto redeem(RedeemPairingCodeRequest r);
}
