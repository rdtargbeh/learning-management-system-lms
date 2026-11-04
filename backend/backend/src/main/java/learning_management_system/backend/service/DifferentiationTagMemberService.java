package learning_management_system.backend.service;

import learning_management_system.backend.dto.AddTagMembersRequest;
import learning_management_system.backend.dto.DifferentiationTagMemberDto;

import java.util.List;
import java.util.UUID;

/** differentiation_tag_member: which students carry a tag. Only enrolled students of the course can be tagged. */
public interface DifferentiationTagMemberService {

    List<DifferentiationTagMemberDto> list(UUID tagId);

    /** Returns the tag's full member list. */
    List<DifferentiationTagMemberDto> add(UUID tagId, AddTagMembersRequest r);

    void remove(UUID tagId, UUID userId);

    /** Internal: does the user carry the tag? */
    boolean isTagged(UUID tagId, UUID userId);
}
