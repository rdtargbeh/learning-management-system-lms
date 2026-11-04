package learning_management_system.backend.dto;

import java.util.UUID;

/**
 * Join a full section's waitlist. Students omit userId (they join themselves). autoEnrollWhenAvailable (default
 * true) enrolls automatically when a seat frees up; otherwise the student gets an offer to accept.
 */
public record JoinWaitlistRequest(UUID userId, Boolean autoEnrollWhenAvailable) {}
