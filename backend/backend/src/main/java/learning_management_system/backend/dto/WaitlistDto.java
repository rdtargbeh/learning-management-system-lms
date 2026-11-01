package learning_management_system.backend.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.WaitlistStatus;

/** A place on a section's waitlist (table waitlist). */
public record WaitlistDto(UUID waitlistId, UUID sectionId, UUID userId, String username, String displayName, Integer position,
                          WaitlistStatus waitlistStatus, LocalDate dateAdded, OffsetDateTime dateOffered,
                          OffsetDateTime dateOfferedResponse, String offerResponse, Boolean autoEnrollWhenAvailable) {}
