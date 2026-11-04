package learning_management_system.backend.service;

import learning_management_system.backend.dto.DifferentiationTagDto;
import learning_management_system.backend.dto.DifferentiationTagRequest;

import java.util.List;
import java.util.UUID;

/**
 * differentiation_tag: staff-only labels for students of a course (e.g. "Needs extra practice"), used to give tagged
 * students their own assignment dates. Students never see tags. The course's teachers manage them; TAs can read them.
 */
public interface DifferentiationTagService {

    List<DifferentiationTagDto> list(UUID courseId);

    DifferentiationTagDto create(UUID courseId, DifferentiationTagRequest r);

    DifferentiationTagDto update(UUID tagId, DifferentiationTagRequest r);

    /** Also removes the tag's assignment overrides. */
    void delete(UUID tagId);
}
