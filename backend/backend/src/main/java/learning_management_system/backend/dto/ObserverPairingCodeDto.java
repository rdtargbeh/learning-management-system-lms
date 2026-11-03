package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A pairing code a student gives to a parent or mentor (table observer_pairing_code). The code itself is shown only
 * when it is created; afterwards only whether and by whom it was used.
 */
public record ObserverPairingCodeDto(UUID pairingCodeId, UUID studentUserId, String code, OffsetDateTime dateExpires, boolean used,
                                     UUID usedBy, OffsetDateTime dateUsed, OffsetDateTime dateCreated) {}
