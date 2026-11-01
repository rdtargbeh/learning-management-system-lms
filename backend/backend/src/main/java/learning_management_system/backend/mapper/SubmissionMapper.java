package learning_management_system.backend.mapper;

import learning_management_system.backend.dto.SubmissionDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.GradingStatus;
import learning_management_system.backend.enums.SubmissionStatus;
import learning_management_system.backend.repository.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubmissionMapper {

    private final AssignmentRepository assignmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final ProctoringSessionRepository proctoringSessionRepository;

    public SubmissionMapper(AssignmentRepository assignmentRepository, AssessmentRepository assessmentRepository, UserRepository userRepository, AttachmentRepository attachmentRepository,
                            ProctoringSessionRepository proctoringSessionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.proctoringSessionRepository = proctoringSessionRepository;
    }

    /**
     * Converts a `Submission` entity to a `SubmissionDto`.
     *
     * @param submission The `Submission` entity.
     * @return The corresponding `SubmissionDto`.
     */
    public SubmissionDto toDto(Submission submission) {
        if (submission == null) {
            return null;
        }

        SubmissionDto dto = new SubmissionDto();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setAssignmentId(
                submission.getAssignment() != null ? submission.getAssignment().getAssignmentId() : null);
        dto.setAssessmentId(
                submission.getAssessment() != null ? submission.getAssessment().getAssessmentId() : null);
        dto.setStudentId(
                submission.getStudent() != null ? submission.getStudent().getUserId() : null);
        dto.setStartTime(submission.getStartTime());
        dto.setEndTime(submission.getEndTime());
        dto.setTimeTaken(submission.getTimeTaken());
        dto.setMarksObtained(submission.getMarksObtained());
        dto.setStatus(submission.getStatus().name());
        dto.setGradingStatus(
                submission.getGradingStatus() != null ? submission.getGradingStatus().name() : null);
        dto.setFeedback(submission.getFeedback());
        dto.setRubricDetails(submission.getRubricDetails());
        dto.setGradeComments(submission.getGradeComments());
        dto.setGradeLevel(submission.getGradeLevel());
        dto.setTimePerQuestion(submission.getTimePerQuestion());
        dto.setScoreDistribution(submission.getScoreDistribution());
        dto.setPlagiarismScore(submission.getPlagiarismScore());
        dto.setAnalysisReportUrl(submission.getAnalysisReportUrl());
        dto.setFlaggedForPlagiarism(submission.getFlaggedForPlagiarism());
        dto.setAttemptNumber(submission.getAttemptNumber());
        dto.setRetryCount(submission.getRetryCount());
        dto.setAutoGraded(submission.getAutoGraded());
        dto.setSubmittedContent(submission.getSubmittedContent());
        dto.setMetadata(submission.getMetadata());
        dto.setFinalized(submission.getFinalized());
        dto.setReviewed(submission.getReviewed());
        dto.setSubmissionDate(submission.getSubmissionDate());
        dto.setDateCreated(submission.getDateCreated());
        dto.setDateUpdated(submission.getDateUpdated());
        dto.setProctoringSessionId(
                submission.getProctoringSession() != null
                        ? submission.getProctoringSession().getProctoringSessionId()
                        : null);
        dto.setAttachmentIds(submission.getAttachments() != null
                ? submission.getAttachments().stream()
                .map(Attachment::getAttachmentId)
                .collect(Collectors.toList())
                : null);

        return dto;
    }

    /**
     * Converts a `SubmissionDto` to a `Submission` entity.
     *
     * @param dto The `SubmissionDto`.
     * @return The corresponding `Submission` entity.
     */
    public Submission toEntity(SubmissionDto dto) {
        if (dto == null) {
            return null;
        }

        Submission submission = new Submission();
        submission.setSubmissionId(dto.getSubmissionId());

        // Fetch and set assignment
        if (dto.getAssignmentId() != null) {
            Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assignment not found with ID: " + dto.getAssignmentId()));
            submission.setAssignment(assignment);
        }

        // Fetch and set assessment
        if (dto.getAssessmentId() != null) {
            Assessment assessment = assessmentRepository.findById(dto.getAssessmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Assessment not found with ID: " + dto.getAssessmentId()));
            submission.setAssessment(assessment);
        }

        // Fetch and set student
        if (dto.getStudentId() != null) {
            User student = userRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Student not found with ID: " + dto.getStudentId()));
            submission.setStudent(student);
        }

        submission.setStartTime(dto.getStartTime());
        submission.setEndTime(dto.getEndTime());
        submission.setTimeTaken(dto.getTimeTaken());
        submission.setMarksObtained(dto.getMarksObtained());
        submission.setStatus(SubmissionStatus.valueOf(dto.getStatus()));
        submission.setGradingStatus(
                dto.getGradingStatus() != null
                        ? GradingStatus.valueOf(dto.getGradingStatus())
                        : null);
        submission.setFeedback(dto.getFeedback());
        submission.setRubricDetails(dto.getRubricDetails());
        submission.setGradeComments(dto.getGradeComments());
        submission.setGradeLevel(dto.getGradeLevel());
        submission.setTimePerQuestion(dto.getTimePerQuestion());
        submission.setScoreDistribution(dto.getScoreDistribution());
        submission.setPlagiarismScore(dto.getPlagiarismScore());
        submission.setAnalysisReportUrl(dto.getAnalysisReportUrl());
        submission.setFlaggedForPlagiarism(dto.getFlaggedForPlagiarism());
        submission.setAttemptNumber(dto.getAttemptNumber());
        submission.setRetryCount(dto.getRetryCount());
        submission.setAutoGraded(dto.getAutoGraded());
        submission.setSubmittedContent(dto.getSubmittedContent());
        submission.setMetadata(dto.getMetadata());
        submission.setFinalized(dto.getFinalized());
        submission.setReviewed(dto.getReviewed());
        submission.setSubmissionDate(dto.getSubmissionDate());
        submission.setDateCreated(dto.getDateCreated());
        submission.setDateUpdated(dto.getDateUpdated());

        // Fetch and set proctoring session
        if (dto.getProctoringSessionId() != null) {
            ProctoringSession proctoringSession = proctoringSessionRepository.findById(dto.getProctoringSessionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Proctoring session not found with ID: " + dto.getProctoringSessionId()));
            submission.setProctoringSession(proctoringSession);
        }

        // Fetch and set attachments
        if (dto.getAttachmentIds() != null) {
            List<Attachment> attachments = attachmentRepository.findAllById(dto.getAttachmentIds());
            submission.setAttachments(attachments);
        }

        return submission;
    }

}

