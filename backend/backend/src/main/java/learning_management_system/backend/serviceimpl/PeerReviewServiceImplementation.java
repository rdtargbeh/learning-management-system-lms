package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignPeerReviewsRequest;
import learning_management_system.backend.dto.PeerReviewDto;
import learning_management_system.backend.dto.SubmitPeerReviewRequest;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentPeerReview;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.enums.AssignmentPeerReviewStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.AssignmentMapper;
import learning_management_system.backend.repository.AssignmentPeerReviewRepository;
import learning_management_system.backend.repository.AssignmentRepository;
import learning_management_system.backend.repository.AssignmentSubmissionRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.PeerReviewService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PeerReviewServiceImplementation implements PeerReviewService {
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final AssignmentPeerReviewRepository reviews;
    private final CourseRepository courses;
    private final UserService userService;
    private final CourseAccess access;
    private final AssignmentMapper assignmentMapper;

    public PeerReviewServiceImplementation(AssignmentRepository assignments, AssignmentSubmissionRepository submissions,
                                           AssignmentPeerReviewRepository reviews, CourseRepository courses,
                                           UserService userService, CourseAccess access, AssignmentMapper assignmentMapper) {
        this.assignments = assignments;
        this.submissions = submissions;
        this.reviews = reviews;
        this.courses = courses;
        this.userService = userService;
        this.access = access;
        this.assignmentMapper = assignmentMapper;
    }

    /**
     * Students in random order; student i reviews the next n students in the circle, skipping themselves and group mates.
     * Existing reviews are kept, so running it again only fills gaps.
     */
    @Transactional
    @Override
    public List<PeerReviewDto> assign(UUID assignmentId, AssignPeerReviewsRequest r) {
        Assignment a = load(assignmentId);
        access.requireCourseEditor(a.getCourseId());
        Map<UUID, AssignmentSubmission> latest = latestHandedIn(assignmentId);
        List<UUID> authors = new ArrayList<>(latest.keySet());
        if (authors.size() < 2) throw ApiException.conflict("NOT_ENOUGH_SUBMISSIONS", "At least two students must submit before peer reviews");
        int n = r != null && r.reviewsPerStudent() != null ? r.reviewsPerStudent() : a.getPeerReviewCount() != null ? a.getPeerReviewCount() : 1;
        n = Math.min(n, authors.size() - 1);
        boolean anonymous = r != null && Boolean.TRUE.equals(r.anonymous());
        Collections.shuffle(authors);
        OffsetDateTime now = OffsetDateTime.now();
        Map<UUID, Long> already = reviews.findByAssignmentIdOrderByDateAssignedAsc(assignmentId).stream()
                .collect(Collectors.groupingBy(AssignmentPeerReview::getReviewerId, Collectors.counting()));
        for (int i = 0; i < authors.size(); i++) {
            UUID reviewer = authors.get(i);
            int given = already.getOrDefault(reviewer, 0L).intValue();       // running again only fills gaps
            for (int step = 1; step < authors.size() && given < n; step++) {
                AssignmentSubmission work = latest.get(authors.get((i + step) % authors.size()));
                UUID group = latest.get(reviewer).getGroupId();
                if (group != null && group.equals(work.getGroupId())) continue;          // never review your own group's work
                if (reviews.existsByReviewerIdAndSubmissionId(reviewer, work.getSubmissionId())) continue;
                AssignmentPeerReview pr = new AssignmentPeerReview();
                pr.setAssignmentId(assignmentId);
                pr.setReviewerId(reviewer);
                pr.setSubmissionId(work.getSubmissionId());
                pr.setReviewStatus(AssignmentPeerReviewStatus.PENDING);
                pr.setDateAssigned(now);
                pr.setAnonymousReview(anonymous);
                reviews.save(pr);
                given++;
            }
        }
        reviews.flush();
        if (!Boolean.TRUE.equals(a.getPeerReviewRequired())) {
            a.setPeerReviewRequired(true);
            a.setPeerReviewCount(n);
            assignments.saveAndFlush(a);
        }
        return list(assignmentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeerReviewDto> list(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireGrader(a.getCourseId());
        return toDtos(reviews.findByAssignmentIdOrderByDateAssignedAsc(assignmentId), true, true);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeerReviewDto> toDo(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireCourseViewer(courses.findById(a.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        return toDtos(reviews.findByAssignmentIdAndReviewerIdOrderByDateAssignedAsc(assignmentId, Access.ctx().userId()), true, false);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeerReviewDto> received(UUID assignmentId) {
        Assignment a = load(assignmentId);
        access.requireCourseViewer(courses.findById(a.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        List<UUID> mine = submissions.findByAssignmentIdAndUserIdOrderBySubmissionNumberAsc(assignmentId, Access.ctx().userId()).stream()
                .map(AssignmentSubmission::getSubmissionId).toList();
        if (mine.isEmpty()) return List.of();
        List<AssignmentPeerReview> done = reviews.findBySubmissionIdIn(mine).stream()
                .filter(x -> x.getReviewStatus() == AssignmentPeerReviewStatus.SUBMITTED || x.getReviewStatus() == AssignmentPeerReviewStatus.GRADED).toList();
        return toDtos(done, false, false);
    }

    @Transactional
    @Override
    public PeerReviewDto submit(UUID peerReviewId, SubmitPeerReviewRequest r) {
        AssignmentPeerReview pr = reviews.findById(peerReviewId).orElseThrow(() -> ApiException.notFound("Peer review"));
        if (!Access.isSelf(pr.getReviewerId())) throw ApiException.forbidden("This review is assigned to someone else");
        if (pr.getReviewStatus() == AssignmentPeerReviewStatus.GRADED || pr.getReviewStatus() == AssignmentPeerReviewStatus.DISMISSED) {
            throw ApiException.conflict("REVIEW_CLOSED", "This review is " + pr.getReviewStatus());
        }
        pr.setReviewText(r.reviewText().trim());
        pr.setReviewRating(r.reviewRating());
        pr.setReviewStatus(AssignmentPeerReviewStatus.SUBMITTED);
        pr.setDateSubmitted(OffsetDateTime.now());
        reviews.saveAndFlush(pr);
        return toDtos(List.of(pr), true, false).get(0);
    }

    // ---------------------------------------------------------------- helpers

    private Map<UUID, AssignmentSubmission> latestHandedIn(UUID assignmentId) {
        Map<UUID, AssignmentSubmission> out = new LinkedHashMap<>();
        for (AssignmentSubmission s : submissions.findByAssignmentIdOrderByUserIdAscSubmissionNumberAsc(assignmentId)) {
            if (s.getSubmittedAt() != null && ModuleProgressServiceImplementation.HANDED_IN.contains(s.getSubmissionStatus())) out.put(s.getUserId(), s);
        }
        return out;
    }

    /**
     * @param withWork include the work under review (for the reviewer and graders)
     * @param staff    graders always see names; otherwise names are hidden on anonymous reviews
     */
    private List<PeerReviewDto> toDtos(List<AssignmentPeerReview> list, boolean withWork, boolean staff) {
        if (list.isEmpty()) return List.of();
        Map<UUID, AssignmentSubmission> subs = submissions.findAllById(list.stream().map(AssignmentPeerReview::getSubmissionId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(AssignmentSubmission::getSubmissionId, s -> s));
        Set<UUID> people = new HashSet<>();
        list.forEach(x -> people.add(x.getReviewerId()));
        subs.values().forEach(s -> people.add(s.getUserId()));
        Map<UUID, UserSummaryDto> names = userService.summariesById(people);
        return list.stream().map(x -> {
            AssignmentSubmission s = subs.get(x.getSubmissionId());
            boolean hide = !staff && Boolean.TRUE.equals(x.getAnonymousReview());
            String reviewer = hide ? null : name(names.get(x.getReviewerId()));
            UUID revieweeId = s == null || hide ? null : s.getUserId();
            String reviewee = s == null || hide ? null : name(names.get(s.getUserId()));
            return assignmentMapper.toDto(x, reviewer, revieweeId, reviewee,
                    withWork && s != null ? s.getSubmissionText() : null, withWork && s != null ? s.getSubmissionUrl() : null);
        }).map(d -> hideReviewerId(d, staff)).filter(Objects::nonNull).toList();
    }

    /** On an anonymous review the author must not learn who reviewed them. */
    private static PeerReviewDto hideReviewerId(PeerReviewDto d, boolean staff) {
        if (staff || !Boolean.TRUE.equals(d.anonymousReview()) || Access.isSelf(d.reviewerId())) return d;
        return new PeerReviewDto(d.peerReviewId(), d.assignmentId(), null, null, d.submissionId(), d.revieweeId(), d.revieweeName(),
                d.submissionText(), d.submissionUrl(), d.reviewStatus(), d.reviewText(), d.reviewRating(), d.dateAssigned(),
                d.dateSubmitted(), d.anonymousReview());
    }

    private static String name(UserSummaryDto u) {
        return u == null ? null : u.displayName() != null ? u.displayName() : u.username();
    }

    private Assignment load(UUID id) {
        return assignments.findById(id).orElseThrow(() -> ApiException.notFound("Assignment"));
    }
}
