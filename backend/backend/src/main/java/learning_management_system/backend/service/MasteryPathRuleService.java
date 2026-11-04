package learning_management_system.backend.service;

import learning_management_system.backend.dto.CreateMasteryPathRuleRequest;
import learning_management_system.backend.dto.MasteryPathRuleDto;
import learning_management_system.backend.dto.MyMasteryPathDto;

import java.util.List;
import java.util.UUID;

/**
 * mastery_path_rule: Canvas-style mastery paths. The posted score on a trigger assignment puts a student into one of the
 * path's ranges, which releases that range's items. Items placed on a path stay locked for students until their score
 * puts them in a range that holds the item. The course's teachers build paths.
 */
public interface MasteryPathRuleService {

    List<MasteryPathRuleDto> list(UUID courseId);

    MasteryPathRuleDto create(UUID courseId, CreateMasteryPathRuleRequest r);

    MasteryPathRuleDto get(UUID ruleId);

    void delete(UUID ruleId);

    /** The caller's place on the path (students). */
    MyMasteryPathDto mine(UUID ruleId);

    /** Internal: why a student cannot open an item that sits on a path, or null. */
    String lockReason(UUID courseId, String itemType, UUID itemId, UUID userId);
}
