package learning_management_system.backend.service;

import learning_management_system.backend.dto.MasteryPathRangeDto;
import learning_management_system.backend.dto.MasteryPathRangeRequest;

import java.util.UUID;

/** mastery_path_range: the score ranges of a mastery path (they may touch but not overlap). */
public interface MasteryPathRangeService {

    MasteryPathRangeDto add(UUID ruleId, MasteryPathRangeRequest r);

    MasteryPathRangeDto update(UUID rangeId, MasteryPathRangeRequest r);

    /** Also removes the range's items. */
    void delete(UUID rangeId);
}
