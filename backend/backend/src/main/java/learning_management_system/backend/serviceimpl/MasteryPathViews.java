package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.MasteryPathItemDto;
import learning_management_system.backend.dto.MasteryPathRangeDto;
import learning_management_system.backend.dto.MasteryPathRuleDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.CourseAccess;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Builds mastery-path views and holds the rules shared by the rule, range and item services. */
@Component
class MasteryPathViews {
    final MasteryPathRuleRepository rules;
    final MasteryPathRangeRepository ranges;
    final MasteryPathSetItemRepository items;
    final AssignmentRepository assignments;
    final ContentRepository contents;
    final CourseAccess access;

    MasteryPathViews(MasteryPathRuleRepository rules, MasteryPathRangeRepository ranges, MasteryPathSetItemRepository items,
                     AssignmentRepository assignments, ContentRepository contents, CourseAccess access) {
        this.rules = rules;
        this.ranges = ranges;
        this.items = items;
        this.assignments = assignments;
        this.contents = contents;
        this.access = access;
    }

    MasteryPathRuleDto rule(MasteryPathRule r) {
        return new MasteryPathRuleDto(r.getRuleId(), r.getCourseId(), r.getTriggerAssignmentId(), assignmentTitle(r.getTriggerAssignmentId()),
                ranges.findByRuleIdOrderByLowerBoundPercentAsc(r.getRuleId()).stream().map(this::range).toList(), r.getDateCreated());
    }

    MasteryPathRangeDto range(MasteryPathRange g) {
        return new MasteryPathRangeDto(g.getRangeId(), g.getRuleId(), g.getLowerBoundPercent(), g.getUpperBoundPercent(), g.getDisplayOrder(),
                items.findByRangeIdOrderBySetNumberAscDisplayOrderAsc(g.getRangeId()).stream().map(this::item).toList());
    }

    MasteryPathItemDto item(MasteryPathSetItem i) {
        boolean content = i.getContentId() != null;
        UUID id = content ? i.getContentId() : i.getAssignmentId();
        String title = content ? contents.findById(id).map(Content::getContentTitle).orElse(null) : assignmentTitle(id);
        return new MasteryPathItemDto(i.getSetItemId(), i.getRangeId(), i.getSetNumber(), content ? "CONTENT" : "ASSIGNMENT", id, title, i.getDisplayOrder());
    }

    String assignmentTitle(UUID id) {
        return id == null ? null : assignments.findById(id).map(Assignment::getAssignmentTitle).orElse(null);
    }

    /** Does a score fall in the range? Lower bound included; upper excluded except for the path's top range. */
    boolean inRange(BigDecimal score, MasteryPathRange g, List<MasteryPathRange> all) {
        if (score.compareTo(g.getLowerBoundPercent()) < 0) return false;
        if (score.compareTo(g.getUpperBoundPercent()) < 0) return true;
        BigDecimal top = all.stream().map(MasteryPathRange::getUpperBoundPercent).max(BigDecimal::compareTo).orElse(g.getUpperBoundPercent());
        return g.getUpperBoundPercent().compareTo(top) == 0 && score.compareTo(top) >= 0;
    }

    MasteryPathRule loadRule(UUID id) {
        return rules.findById(id).orElseThrow(() -> ApiException.notFound("Mastery path"));
    }

    MasteryPathRange loadRange(UUID id) {
        return ranges.findById(id).orElseThrow(() -> ApiException.notFound("Range"));
    }
}
