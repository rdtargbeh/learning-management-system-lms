package learning_management_system.backend.mapper;

import learning_management_system.backend.config.MapStructConfig;
import learning_management_system.backend.dto.AssignmentDto;
import learning_management_system.backend.dto.AssignmentOverrideDto;
import learning_management_system.backend.dto.CommentLibraryItemDto;
import learning_management_system.backend.dto.CreateAssignmentOverrideRequest;
import learning_management_system.backend.dto.CreateAssignmentRequest;
import learning_management_system.backend.dto.FeedbackDto;
import learning_management_system.backend.dto.PeerReviewDto;
import learning_management_system.backend.dto.SubmissionDto;
import learning_management_system.backend.dto.UpdateAssignmentRequest;
import learning_management_system.backend.entity.Assignment;
import learning_management_system.backend.entity.AssignmentOverride;
import learning_management_system.backend.entity.AssignmentPeerReview;
import learning_management_system.backend.entity.AssignmentSubmission;
import learning_management_system.backend.entity.CommentLibraryItem;
import learning_management_system.backend.entity.Feedback;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

/** assignment, assignment_override, assignment_submission, feedback, assignment_peer_review, comment_library_item. */
@Mapper(config = MapStructConfig.class)
public interface AssignmentMapper {

    AssignmentDto toDto(Assignment assignment);

    Assignment toEntity(CreateAssignmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateAssignmentRequest request, @MappingTarget Assignment assignment);

    AssignmentOverrideDto toDto(AssignmentOverride override);

    AssignmentOverride toEntity(CreateAssignmentOverrideRequest request);

    FeedbackDto toDto(Feedback feedback);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "displayName", source = "displayName")
    @Mapping(target = "feedback", source = "feedback")
    @Mapping(target = "dateUpdated", source = "submission.dateUpdated")
    SubmissionDto toDto(AssignmentSubmission submission, String username, String displayName, FeedbackDto feedback);

    @Mapping(target = "reviewerName", source = "reviewerName")
    @Mapping(target = "revieweeId", source = "revieweeId")
    @Mapping(target = "revieweeName", source = "revieweeName")
    @Mapping(target = "submissionText", source = "submissionText")
    @Mapping(target = "submissionUrl", source = "submissionUrl")
    PeerReviewDto toDto(AssignmentPeerReview review, String reviewerName, UUID revieweeId, String revieweeName,
                        String submissionText, String submissionUrl);

    CommentLibraryItemDto toDto(CommentLibraryItem item);
}
