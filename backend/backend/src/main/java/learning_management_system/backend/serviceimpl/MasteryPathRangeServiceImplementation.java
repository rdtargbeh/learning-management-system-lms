package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.MasteryPathRangeDto;
import learning_management_system.backend.dto.MasteryPathRangeRequest;
import learning_management_system.backend.entity.MasteryPathRange;
import learning_management_system.backend.entity.MasteryPathRule;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.service.MasteryPathRangeService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MasteryPathRangeServiceImplementation implements MasteryPathRangeService {
    private final MasteryPathViews v;
    private final EntityRefresher refresher;

    public MasteryPathRangeServiceImplementation(MasteryPathViews v, EntityRefresher refresher) {
        this.v = v;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public MasteryPathRangeDto add(UUID ruleId, MasteryPathRangeRequest r) {
        MasteryPathRule rule = v.loadRule(ruleId);
        v.access.requireCourseEditor(rule.getCourseId());
        if (r.lowerBoundPercent() == null || r.upperBoundPercent() == null) throw ApiException.badRequest("BOUNDS_REQUIRED", "Both bounds are required");
        MasteryPathRange g = new MasteryPathRange();
        g.setRuleId(ruleId);
        g.setDisplayOrder(r.displayOrder() == null ? v.ranges.findByRuleIdOrderByLowerBoundPercentAsc(ruleId).size() : r.displayOrder());
        place(g, r.lowerBoundPercent(), r.upperBoundPercent());
        return v.range(refresher.refresh(v.ranges.saveAndFlush(g)));
    }

    @Transactional
    @Override
    public MasteryPathRangeDto update(UUID rangeId, MasteryPathRangeRequest r) {
        MasteryPathRange g = v.loadRange(rangeId);
        v.access.requireCourseEditor(v.loadRule(g.getRuleId()).getCourseId());
        place(g, r.lowerBoundPercent() != null ? r.lowerBoundPercent() : g.getLowerBoundPercent(),
                r.upperBoundPercent() != null ? r.upperBoundPercent() : g.getUpperBoundPercent());
        if (r.displayOrder() != null) g.setDisplayOrder(r.displayOrder());
        return v.range(v.ranges.saveAndFlush(g));
    }

    @Transactional
    @Override
    public void delete(UUID rangeId) {
        MasteryPathRange g = v.loadRange(rangeId);
        v.access.requireCourseEditor(v.loadRule(g.getRuleId()).getCourseId());
        v.ranges.delete(g);
    }

    /** Bounds must be ordered and must not overlap another range of the path (touching is fine). */
    private void place(MasteryPathRange g, BigDecimal lower, BigDecimal upper) {
        if (upper.compareTo(lower) <= 0) throw ApiException.badRequest("INVALID_BOUNDS", "upperBoundPercent must be above lowerBoundPercent");
        for (MasteryPathRange o : v.ranges.findByRuleIdOrderByLowerBoundPercentAsc(g.getRuleId())) {
            if (o.getRangeId().equals(g.getRangeId())) continue;
            if (lower.compareTo(o.getUpperBoundPercent()) < 0 && o.getLowerBoundPercent().compareTo(upper) < 0) {
                throw ApiException.conflict("RANGES_OVERLAP", "Overlaps the range " + o.getLowerBoundPercent().stripTrailingZeros().toPlainString()
                        + "-" + o.getUpperBoundPercent().stripTrailingZeros().toPlainString() + "%");
            }
        }
        g.setLowerBoundPercent(lower);
        g.setUpperBoundPercent(upper);
    }
}
