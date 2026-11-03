package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.LearningOutcomeDto;
import learning_management_system.backend.dto.LearningOutcomeRequest;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.LearningOutcome;
import learning_management_system.backend.entity.OrgUnit;
import learning_management_system.backend.enums.CalculationMethod;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.LearningOutcomeMapper;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.LearningOutcomeRepository;
import learning_management_system.backend.repository.OrgUnitRepository;
import learning_management_system.backend.repository.OutcomeResultRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.LearningOutcomeService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
public class LearningOutcomeServiceImplementation implements LearningOutcomeService {
    private final LearningOutcomeRepository outcomes;
    private final OutcomeResultRepository results;
    private final CourseRepository courses;
    private final OrgUnitRepository orgUnits;
    private final LearningOutcomeMapper mapper;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public LearningOutcomeServiceImplementation(LearningOutcomeRepository outcomes, OutcomeResultRepository results, CourseRepository courses,
                                                OrgUnitRepository orgUnits, LearningOutcomeMapper mapper, CourseAccess access,
                                                EntityRefresher refresher) {
        this.outcomes = outcomes;
        this.results = results;
        this.courses = courses;
        this.orgUnits = orgUnits;
        this.mapper = mapper;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<LearningOutcomeDto> forCourse(UUID courseId, boolean includeInherited, boolean includeInactive) {
        Course c = course(courseId);
        access.requireCourseViewer(c);
        boolean staffView = access.canSeeEverything() || access.isCourseStaff(courseId);
        Stream<LearningOutcome> own = outcomes.findByCourseIdOrderByOutcomeNumberAsc(courseId).stream();
        Stream<LearningOutcome> inherited = includeInherited && c.getOrgUnitId() != null
                ? outcomes.findByCourseIdIsNullAndOrgUnitIdInOrderByOutcomeNumberAsc(ancestors(c.getOrgUnitId())).stream() : Stream.empty();
        return Stream.concat(inherited, own)
                .filter(o -> Boolean.TRUE.equals(o.getIsActive()) || (includeInactive && staffView))
                .map(mapper::toDto).toList();
    }

    @Transactional
    @Override
    public LearningOutcomeDto createForCourse(UUID courseId, LearningOutcomeRequest r) {
        course(courseId);
        access.requireCourseEditor(courseId);
        LearningOutcome o = new LearningOutcome();
        o.setCourseId(courseId);
        int number = r.outcomeNumber() != null ? r.outcomeNumber() : outcomes.maxNumberInCourse(courseId) + 1;
        if (r.outcomeNumber() != null && outcomes.existsByCourseIdAndOutcomeNumber(courseId, number)) {
            throw ApiException.conflict("OUTCOME_NUMBER_TAKEN", "Outcome " + number + " already exists in this course");
        }
        return create(o, number, r);
    }

    @Transactional(readOnly = true)
    @Override
    public List<LearningOutcomeDto> forOrgUnit(UUID orgUnitId, boolean includeInactive) {
        Access.requireTenant();
        orgUnit(orgUnitId);
        boolean manager = Access.hasTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        return outcomes.findByCourseIdIsNullAndOrgUnitIdInOrderByOutcomeNumberAsc(List.of(orgUnitId)).stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsActive()) || (includeInactive && manager))
                .map(mapper::toDto).toList();
    }

    @Transactional
    @Override
    public LearningOutcomeDto createForOrgUnit(UUID orgUnitId, LearningOutcomeRequest r) {
        Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
        orgUnit(orgUnitId);
        LearningOutcome o = new LearningOutcome();
        o.setOrgUnitId(orgUnitId);
        int number = r.outcomeNumber() != null ? r.outcomeNumber() : outcomes.maxNumberInOrgUnit(orgUnitId) + 1;
        if (r.outcomeNumber() != null && outcomes.existsByCourseIdIsNullAndOrgUnitIdAndOutcomeNumber(orgUnitId, number)) {
            throw ApiException.conflict("OUTCOME_NUMBER_TAKEN", "Outcome " + number + " already exists in this unit");
        }
        return create(o, number, r);
    }

    @Transactional(readOnly = true)
    @Override
    public LearningOutcomeDto get(UUID outcomeId) {
        LearningOutcome o = load(outcomeId);
        if (o.getCourseId() != null) {
            Course c = course(o.getCourseId());
            access.requireCourseViewer(c);
            if (!Boolean.TRUE.equals(o.getIsActive()) && !access.canSeeEverything() && !access.isCourseStaff(c.getCourseId())) {
                throw ApiException.notFound("Outcome");
            }
        } else {
            Access.requireTenant();
        }
        return mapper.toDto(o);
    }

    @Transactional
    @Override
    public LearningOutcomeDto update(UUID outcomeId, LearningOutcomeRequest r) {
        LearningOutcome o = load(outcomeId);
        requireEditor(o);
        if (r.outcomeNumber() != null && !r.outcomeNumber().equals(o.getOutcomeNumber())) {
            boolean taken = o.getCourseId() != null ? outcomes.existsByCourseIdAndOutcomeNumber(o.getCourseId(), r.outcomeNumber())
                    : outcomes.existsByCourseIdIsNullAndOrgUnitIdAndOutcomeNumber(o.getOrgUnitId(), r.outcomeNumber());
            if (taken) throw ApiException.conflict("OUTCOME_NUMBER_TAKEN", "Outcome " + r.outcomeNumber() + " already exists");
        }
        CalculationMethod before = o.getCalculationMethod();
        mapper.updateEntity(r, o);
        if (r.outcomeDescription() != null) o.setOutcomeDescription(r.outcomeDescription().trim());
        // a new method without a new number gets that method's default
        if (r.calculationMethod() != null && r.calculationMethod() != before && r.calculationInt() == null) o.setCalculationInt(null);
        normaliseCalculation(o);
        outcomes.saveAndFlush(o);
        return mapper.toDto(refresher.refresh(o));
    }

    @Transactional
    @Override
    public void delete(UUID outcomeId) {
        LearningOutcome o = load(outcomeId);
        requireEditor(o);
        if (results.existsByOutcomeId(outcomeId)) {
            throw ApiException.conflict("OUTCOME_IN_USE", "Students have results for this outcome; deactivate it instead");
        }
        outcomes.delete(o);
    }

    // ---------------------------------------------------------------- helpers

    private LearningOutcomeDto create(LearningOutcome o, int number, LearningOutcomeRequest r) {
        if (r.outcomeDescription() == null || r.outcomeDescription().isBlank()) {
            throw ApiException.badRequest("DESCRIPTION_REQUIRED", "outcomeDescription is required");
        }
        mapper.updateEntity(r, o);
        o.setOutcomeNumber(number);
        o.setOutcomeDescription(r.outcomeDescription().trim());
        if (o.getCalculationMethod() == null) o.setCalculationMethod(CalculationMethod.DECAYING_AVERAGE);
        if (o.getIsActive() == null) o.setIsActive(true);
        if (o.getIsPrimary() == null) o.setIsPrimary(false);
        normaliseCalculation(o);
        return mapper.toDto(refresher.refresh(outcomes.saveAndFlush(o)));
    }

    /** DECAYING_AVERAGE 1-99 (default 65), N_MASTERY 1-10 (default 5); other methods take no number. */
    private static void normaliseCalculation(LearningOutcome o) {
        Integer n = o.getCalculationInt();
        switch (o.getCalculationMethod()) {
            case DECAYING_AVERAGE -> {
                if (n == null) o.setCalculationInt(65);
                else if (n < 1 || n > 99) throw ApiException.badRequest("INVALID_CALCULATION", "DECAYING_AVERAGE takes a weight from 1 to 99");
            }
            case N_MASTERY -> {
                if (n == null) o.setCalculationInt(5);
                else if (n < 1 || n > 10) throw ApiException.badRequest("INVALID_CALCULATION", "N_MASTERY takes a count from 1 to 10");
            }
            default -> {
                if (n != null) throw ApiException.badRequest("INVALID_CALCULATION", o.getCalculationMethod() + " takes no calculationInt");
            }
        }
    }

    private void requireEditor(LearningOutcome o) {
        if (o.getCourseId() != null) access.requireCourseEditor(o.getCourseId());
        else Access.requireTenantPermission(Permissions.ACADEMIC_STRUCTURE_MANAGE);
    }

    /** The unit and every unit above it (cycles are prevented when units are saved). */
    private List<UUID> ancestors(UUID orgUnitId) {
        List<UUID> chain = new ArrayList<>();
        UUID id = orgUnitId;
        while (id != null && !chain.contains(id)) {
            chain.add(id);
            id = orgUnits.findById(id).map(OrgUnit::getParentOrgUnitId).orElse(null);
        }
        return chain;
    }

    private LearningOutcome load(UUID id) {
        return outcomes.findById(id).orElseThrow(() -> ApiException.notFound("Outcome"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }

    private OrgUnit orgUnit(UUID id) {
        return orgUnits.findById(id).orElseThrow(() -> ApiException.notFound("Org unit"));
    }
}
