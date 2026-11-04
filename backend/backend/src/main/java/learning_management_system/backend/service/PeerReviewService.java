package learning_management_system.backend.service;

import learning_management_system.backend.dto.AssignPeerReviewsRequest;
import learning_management_system.backend.dto.PeerReviewDto;
import learning_management_system.backend.dto.SubmitPeerReviewRequest;

import java.util.List;
import java.util.UUID;

/**
 * Peer reviews: the teacher hands them out once students have submitted (each student reviews a few classmates, never
 * themselves or their own group); students write a comment and a 1-5 rating; the author sees reviews of their work.
 */
public interface PeerReviewService {

    /** Creates the missing reviews; returns every review of the assignment. */
    List<PeerReviewDto> assign(UUID assignmentId, AssignPeerReviewsRequest r);

    /** All reviews of the assignment (graders). */
    List<PeerReviewDto> list(UUID assignmentId);

    /** Reviews the caller has to write, with the work to review. */
    List<PeerReviewDto> toDo(UUID assignmentId);

    /** Finished reviews of the caller's own work. */
    List<PeerReviewDto> received(UUID assignmentId);

    PeerReviewDto submit(UUID peerReviewId, SubmitPeerReviewRequest r);
}
