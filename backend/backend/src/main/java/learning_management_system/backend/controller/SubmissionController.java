package learning_management_system.backend.controller;

import learning_management_system.backend.dto.GradeSubmissionRequest;
import learning_management_system.backend.dto.StudentSubmissionSummaryDto;
import learning_management_system.backend.dto.SubmissionDto;
import learning_management_system.backend.dto.SubmitAssignmentRequest;
import learning_management_system.backend.service.SubmissionService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Submissions, grading, posting, excusing and reassigning. */
@RestController
@RequestMapping("/api/v1")
public class SubmissionController {
    private final SubmissionService submissions;

    public SubmissionController(SubmissionService submissions) { this.submissions = submissions; }

    /** Hand in (or save a draft with draft = true). */
    @PostMapping("/assignments/{assignmentId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submit(@PathVariable UUID assignmentId, @Valid @RequestBody SubmitAssignmentRequest r) {
        return submissions.submit(assignmentId, r);
    }

    @GetMapping("/assignments/{assignmentId}/submissions/mine")
    public List<SubmissionDto> mine(@PathVariable UUID assignmentId) { return submissions.mine(assignmentId); }

    /** Every student with their latest attempt, including missing work (graders). */
    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<StudentSubmissionSummaryDto> forGrading(@PathVariable UUID assignmentId) { return submissions.forGrading(assignmentId); }

    @PostMapping("/assignments/{assignmentId}/post-grades")
    public Map<String, Integer> postGrades(@PathVariable UUID assignmentId) {
        return Map.of("posted", submissions.postGrades(assignmentId));
    }

    @PutMapping("/assignments/{assignmentId}/students/{userId}/excuse")
    public SubmissionDto excuse(@PathVariable UUID assignmentId, @PathVariable UUID userId) {
        return submissions.setExcused(assignmentId, userId, true);
    }

    @DeleteMapping("/assignments/{assignmentId}/students/{userId}/excuse")
    public SubmissionDto unexcuse(@PathVariable UUID assignmentId, @PathVariable UUID userId) {
        return submissions.setExcused(assignmentId, userId, false);
    }

    @GetMapping("/submissions/{submissionId}")
    public SubmissionDto get(@PathVariable UUID submissionId) { return submissions.get(submissionId); }

    @PostMapping("/submissions/{submissionId}/grade")
    public SubmissionDto grade(@PathVariable UUID submissionId, @Valid @RequestBody GradeSubmissionRequest r) {
        return submissions.grade(submissionId, r);
    }

    @PostMapping("/submissions/{submissionId}/reassign")
    public SubmissionDto reassign(@PathVariable UUID submissionId) { return submissions.reassign(submissionId); }
}
