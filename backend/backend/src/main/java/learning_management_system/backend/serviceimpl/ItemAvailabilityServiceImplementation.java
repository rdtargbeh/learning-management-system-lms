package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.enums.TargetType;
import learning_management_system.backend.service.ItemAvailabilityService;
import learning_management_system.backend.service.MasteryPathRuleService;
import learning_management_system.backend.service.ReleaseConditionService;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ItemAvailabilityServiceImplementation implements ItemAvailabilityService {
    private final ReleaseConditionService conditions;
    private final MasteryPathRuleService masteryPaths;

    public ItemAvailabilityServiceImplementation(ReleaseConditionService conditions, MasteryPathRuleService masteryPaths) {
        this.conditions = conditions;
        this.masteryPaths = masteryPaths;
    }

    @Override
    public String lockReason(UUID courseId, TargetType type, UUID itemId, UUID moduleId, UUID userId) {
        String r = conditions.lockReason(courseId, type, itemId, userId);
        if (r != null) return r;
        if (moduleId != null && type != TargetType.MODULE) {
            r = conditions.lockReason(courseId, TargetType.MODULE, moduleId, userId);
            if (r != null) return "This module is not available yet: " + r;
        }
        if (type == TargetType.CONTENT || type == TargetType.ASSIGNMENT) return masteryPaths.lockReason(courseId, type.name(), itemId, userId);
        return null;
    }
}
