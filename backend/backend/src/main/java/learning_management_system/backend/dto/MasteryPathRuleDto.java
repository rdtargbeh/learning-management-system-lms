package learning_management_system.backend.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** A mastery path (table mastery_path_rule): the score on the trigger assignment decides which range's items a student gets. */
public record MasteryPathRuleDto(UUID ruleId, UUID courseId, UUID triggerAssignmentId, String triggerTitle, List<MasteryPathRangeDto> ranges,
                                 OffsetDateTime dateCreated) {}
