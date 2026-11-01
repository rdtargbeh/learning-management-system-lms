package learning_management_system.backend.service;

import learning_management_system.backend.dto.EnrollmentDto;
import learning_management_system.backend.dto.JoinWaitlistRequest;
import learning_management_system.backend.dto.WaitlistDto;

import java.util.List;
import java.util.UUID;

/**
 * Section waitlists. Students can join a full section of a published course; when a seat frees up the next student is
 * enrolled automatically, or gets an offer to accept or decline. An open offer holds the seat.
 */
public interface WaitlistService {

    List<WaitlistDto> list(UUID sectionId);

    /** The caller's waitlist places. */
    List<WaitlistDto> mine();

    WaitlistDto join(UUID sectionId, JoinWaitlistRequest r);

    WaitlistDto leave(UUID waitlistId);

    EnrollmentDto accept(UUID waitlistId);

    WaitlistDto decline(UUID waitlistId);
}
