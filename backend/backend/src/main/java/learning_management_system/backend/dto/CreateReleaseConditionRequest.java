package learning_management_system.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import learning_management_system.backend.enums.ConditionType;
import learning_management_system.backend.enums.ReleaseConditionSourceType;
import learning_management_system.backend.enums.TargetType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A release condition on a CONTENT, MODULE or ASSIGNMENT target:
 * DATE (dateFrom and/or dateUntil); MEMBER (exactly one of memberUserId, memberGroupId, memberSectionId, memberTagId);
 * PERFORMANCE (ASSIGNMENT source, posted score between minScorePercent and maxScorePercent); SUBMITTED (ASSIGNMENT);
 * VIEWED (CONTENT); COMPLETED (CONTENT marked done, or MODULE completed). Conditions with the same conditionGroup
 * (default 1) must all be met; any one fully met group releases the target.
 */
public record CreateReleaseConditionRequest(@NotNull TargetType targetType, @NotNull UUID targetId, @NotNull ConditionType conditionType,
                                            OffsetDateTime dateFrom, OffsetDateTime dateUntil, UUID memberUserId, UUID memberGroupId,
                                            UUID memberSectionId, UUID memberTagId, ReleaseConditionSourceType sourceType, UUID sourceId,
                                            @DecimalMin("0") @DecimalMax("100") BigDecimal minScorePercent,
                                            @DecimalMin("0") @DecimalMax("100") BigDecimal maxScorePercent, @Positive Integer conditionGroup) {}
