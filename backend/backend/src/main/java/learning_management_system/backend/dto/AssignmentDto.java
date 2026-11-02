package learning_management_system.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import learning_management_system.backend.enums.AssignmentGradingType;
import learning_management_system.backend.enums.AssignmentStatus;
import learning_management_system.backend.enums.AssignmentType;
import learning_management_system.backend.enums.SubmissionType;

/**
 * An assignment (table assignment). Dates are days; an assignment is due at the end of dateDue in the course's
 * time zone.
 */
public record AssignmentDto(UUID assignmentId, UUID courseId, UUID moduleId, String assignmentTitle, String assignmentDescription,
                            String assignmentInstructions, AssignmentType assignmentType, AssignmentStatus assignmentStatus,
                            Boolean isPublished, Integer displayOrder, BigDecimal pointsPossible, AssignmentGradingType gradingType,
                            Boolean omitFromFinalGrade, Boolean isAnonymousGrading, SubmissionType submissionType,
                            Integer maxSubmissions, Boolean lateSubmissionAllowed, BigDecimal lateSubmissionPenaltyPercent,
                            LocalDate dateAvailable, LocalDate dateDue, LocalDate dateClose, Boolean allowGroupSubmission,
                            UUID groupSetId, Boolean gradeGroupIndividually, Boolean peerReviewRequired, Integer peerReviewCount,
                            OffsetDateTime dateCreated, OffsetDateTime dateUpdated) {}
