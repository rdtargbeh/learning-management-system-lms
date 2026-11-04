package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssignPeerReviewsRequest;
import learning_management_system.backend.dto.PeerReviewDto;
import learning_management_system.backend.dto.SubmitPeerReviewRequest;
import learning_management_system.backend.service.PeerReviewService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Peer reviews. */
@RestController
@RequestMapping("/api/v1")
public class PeerReviewController {
    private final PeerReviewService reviews;

    public PeerReviewController(PeerReviewService reviews) { this.reviews = reviews; }

    @PostMapping("/assignments/{assignmentId}/peer-reviews/assign")
    public List<PeerReviewDto> assign(@PathVariable UUID assignmentId, @Valid @RequestBody(required = false) AssignPeerReviewsRequest r) {
        return reviews.assign(assignmentId, r);
    }

    @GetMapping("/assignments/{assignmentId}/peer-reviews")
    public List<PeerReviewDto> list(@PathVariable UUID assignmentId) { return reviews.list(assignmentId); }

    @GetMapping("/assignments/{assignmentId}/peer-reviews/mine")
    public List<PeerReviewDto> toDo(@PathVariable UUID assignmentId) { return reviews.toDo(assignmentId); }

    @GetMapping("/assignments/{assignmentId}/peer-reviews/received")
    public List<PeerReviewDto> received(@PathVariable UUID assignmentId) { return reviews.received(assignmentId); }

    @PostMapping("/peer-reviews/{peerReviewId}/submit")
    public PeerReviewDto submit(@PathVariable UUID peerReviewId, @Valid @RequestBody SubmitPeerReviewRequest r) {
        return reviews.submit(peerReviewId, r);
    }
}
