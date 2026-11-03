package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateMasteryPathRuleRequest;
import learning_management_system.backend.dto.MasteryPathItemDto;
import learning_management_system.backend.dto.MasteryPathRuleDto;
import learning_management_system.backend.dto.MyMasteryPathDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.service.MasteryPathRuleService;
import learning_management_system.backend.utility.EntityRefresher;
import learning_management_system.backend.utility.StudentWorkLookup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasteryPathRuleServiceImplementation implements MasteryPathRuleService {
    private final MasteryPathViews v;
    private final CourseRepository courses;
    private final StudentWorkLookup work;
    private final EntityRefresher refresher;

    public MasteryPathRuleServiceImplementation(MasteryPathViews v, CourseRepository courses, StudentWorkLookup work, EntityRefresher refresher) {
        this.v = v;
        this.courses = courses;
        this.work = work;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<MasteryPathRuleDto> list(UUID courseId) {
        requireStaff(courseId);
        return v.rules.findByCourseIdOrderByDateCreatedAsc(courseId).stream().map(v::rule).toList();
    }

    @Transactional
    @Override
    public MasteryPathRuleDto create(UUID courseId, CreateMasteryPathRuleRequest r) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        v.access.requireCourseEditor(courseId);
        Assignment a = v.assignments.findById(r.triggerAssignmentId()).filter(x -> x.getCourseId().equals(courseId))
                .orElseThrow(() -> ApiException.badRequest("ASSIGNMENT_NOT_IN_COURSE", "The assignment is not part of this course"));
        if (a.getPointsPossible() == null || a.getPointsPossible().signum() <= 0) {
            throw ApiException.badRequest("NOT_GRADED_BY_POINTS", "A mastery path needs an assignment graded by points");
        }
        if (v.rules.existsByTriggerAssignmentId(a.getAssignmentId())) throw ApiException.conflict("PATH_EXISTS", "The assignment already has a mastery path");
        MasteryPathRule rule = new MasteryPathRule();
        rule.setCourseId(courseId);
        rule.setTriggerAssignmentId(a.getAssignmentId());
        return v.rule(refresher.refresh(v.rules.saveAndFlush(rule)));
    }

    @Transactional(readOnly = true)
    @Override
    public MasteryPathRuleDto get(UUID ruleId) {
        MasteryPathRule r = v.loadRule(ruleId);
        requireStaff(r.getCourseId());
        return v.rule(r);
    }

    @Transactional
    @Override
    public void delete(UUID ruleId) {
        MasteryPathRule r = v.loadRule(ruleId);
        v.access.requireCourseEditor(r.getCourseId());
        v.rules.delete(r);                                 // ranges and items go with it
    }

    @Transactional(readOnly = true)
    @Override
    public MyMasteryPathDto mine(UUID ruleId) {
        MasteryPathRule r = v.loadRule(ruleId);
        v.access.requireCourseViewer(courses.findById(r.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        UUID me = Access.ctx().userId();
        BigDecimal score = work.postedPercent(r.getTriggerAssignmentId(), me);
        MasteryPathRange hit = score == null ? null : matching(r, score);
        List<MasteryPathItemDto> released = hit == null ? List.of()
                : v.items.findByRangeIdOrderBySetNumberAscDisplayOrderAsc(hit.getRangeId()).stream().map(v::item).toList();
        return new MyMasteryPathDto(r.getRuleId(), r.getTriggerAssignmentId(), v.assignmentTitle(r.getTriggerAssignmentId()), score,
                hit == null ? null : hit.getRangeId(), released);
    }

    @Override
    public String lockReason(UUID courseId, String itemType, UUID itemId, UUID userId) {
        List<MasteryPathSetItem> placements = "CONTENT".equals(itemType) ? v.items.findByContentId(itemId) : v.items.findByAssignmentId(itemId);
        if (placements.isEmpty()) return null;
        Map<UUID, MasteryPathRange> rangesById = v.ranges.findAllById(placements.stream().map(MasteryPathSetItem::getRangeId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(MasteryPathRange::getRangeId, g -> g));
        String reason = null;
        for (UUID ruleId : rangesById.values().stream().map(MasteryPathRange::getRuleId).collect(Collectors.toCollection(LinkedHashSet::new))) {
            MasteryPathRule rule = v.rules.findById(ruleId).orElse(null);
            if (rule == null || !rule.getCourseId().equals(courseId)) continue;
            BigDecimal score = work.postedPercent(rule.getTriggerAssignmentId(), userId);
            String trigger = v.assignmentTitle(rule.getTriggerAssignmentId());
            if (score == null) { if (reason == null) reason = "Assigned after your grade on \"" + trigger + "\" is posted"; continue; }
            MasteryPathRange hit = matching(rule, score);
            if (hit != null && rangesById.containsKey(hit.getRangeId())) return null;      // on the student's path
            if (reason == null) reason = "Not part of your path after \"" + trigger + "\"";
        }
        return reason;
    }

    private MasteryPathRange matching(MasteryPathRule r, BigDecimal score) {
        List<MasteryPathRange> all = v.ranges.findByRuleIdOrderByLowerBoundPercentAsc(r.getRuleId());
        return all.stream().filter(g -> v.inRange(score, g, all)).findFirst().orElse(null);
    }

    private void requireStaff(UUID courseId) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        Access.requireTenant();
        if (!v.access.canSeeEverything() && !v.access.isCourseStaff(courseId)) throw ApiException.forbidden("Only course staff can see mastery paths");
    }
}
