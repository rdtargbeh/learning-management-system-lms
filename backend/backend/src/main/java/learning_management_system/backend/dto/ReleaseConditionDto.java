package learning_management_system.backend.dto;

import learning_management_system.backend.enums.ConditionType;
import learning_management_system.backend.enums.ReleaseConditionSourceType;
import learning_management_system.backend.enums.TargetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** A rule an item must meet before students can open it (table release_condition). */
public record ReleaseConditionDto(UUID conditionId, UUID courseId, TargetType targetType, UUID targetId, ConditionType conditionType,
                                  OffsetDateTime dateFrom, OffsetDateTime dateUntil, UUID memberUserId, UUID memberGroupId, UUID memberSectionId,
                                  UUID memberTagId, ReleaseConditionSourceType sourceType, UUID sourceId, BigDecimal minScorePercent,
                                  BigDecimal maxScorePercent, Integer conditionGroup, OffsetDateTime dateCreated) {}
