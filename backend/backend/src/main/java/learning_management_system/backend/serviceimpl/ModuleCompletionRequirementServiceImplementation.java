package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.*;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.service.CourseModuleService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static learning_management_system.backend.enums.BlueprintLockAspect.SETTINGS;
import learning_management_system.backend.service.ModuleCompletionRequirementService;

/**
 * module_completion_requirement: what completes a module. Content takes VIEW or MARK_DONE; assignments SUBMIT or
 * MIN_SCORE (points, posted grades). Items must be in the module.
 */
@Service
public class ModuleCompletionRequirementServiceImplementation implements ModuleCompletionRequirementService {
    private final ModuleCompletionRequirementRepository requirements;
    private final CourseModuleRepository modules;
    private final CourseRepository courses;
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final CourseModuleService modulesService;
    private final BlueprintContentLockService blueprintLocks;
    private final CourseAccess access;
    private final learning_management_system.backend.mapper.CourseModuleMapper moduleMapper;

    public ModuleCompletionRequirementServiceImplementation(ModuleCompletionRequirementRepository requirements, CourseModuleRepository modules,
                                                            CourseRepository courses, ContentRepository contents,
                                                            AssignmentRepository assignments, CourseModuleService modulesService,
                                                            BlueprintContentLockService blueprintLocks, CourseAccess access,
                                                            learning_management_system.backend.mapper.CourseModuleMapper moduleMapper) {
        this.requirements = requirements;
        this.modules = modules;
        this.courses = courses;
        this.contents = contents;
        this.assignments = assignments;
        this.modulesService = modulesService;
        this.blueprintLocks = blueprintLocks;
        this.access = access;
        this.moduleMapper = moduleMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ModuleRequirementDto> list(UUID moduleId) {
        CourseModule m = module(moduleId);
        access.requireCourseViewer(courses.findById(m.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        return requirements.findByModuleIdOrderByDisplayOrderAsc(moduleId).stream().map(moduleMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CourseModuleDto add(UUID moduleId, AddModuleRequirementRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        blueprintLocks.requireEditable(m.getCourseId(), BlueprintContentLockObjectType.MODULE, moduleId, java.util.EnumSet.of(SETTINGS));
        if ((r.contentId() == null) == (r.assignmentId() == null)) {
            throw ApiException.badRequest("ONE_ITEM_REQUIRED", "Give either contentId or assignmentId");
        }
        ModuleCompletionRequirement req;
        Integer order;
        if (r.contentId() != null) {
            if (!CourseModuleServiceImplementation.CONTENT_REQUIREMENTS.contains(r.requirementType())) {
                throw ApiException.badRequest("REQUIREMENT_NOT_FOR_CONTENT", "Content items take VIEW or MARK_DONE requirements");
            }
            Content x = contents.findById(r.contentId()).filter(c -> moduleId.equals(c.getModuleId()))
                    .orElseThrow(() -> ApiException.badRequest("ITEM_NOT_IN_MODULE", "The item is not in this module"));
            req = requirements.findByModuleIdAndContentId(moduleId, x.getContentId()).orElseGet(ModuleCompletionRequirement::new);
            req.setContentId(x.getContentId());
            req.setMinScore(null);
            order = x.getDisplayOrder();
        } else {
            if (!CourseModuleServiceImplementation.ASSIGNMENT_REQUIREMENTS.contains(r.requirementType())) {
                throw ApiException.badRequest("REQUIREMENT_NOT_FOR_ASSIGNMENT", "Assignments take SUBMIT or MIN_SCORE requirements");
            }
            Assignment x = assignments.findById(r.assignmentId()).filter(a -> moduleId.equals(a.getModuleId()))
                    .orElseThrow(() -> ApiException.badRequest("ITEM_NOT_IN_MODULE", "The assignment is not in this module"));
            if (r.requirementType() == ModuleCompletionRequirementType.MIN_SCORE) {
                if (r.minScore() == null) throw ApiException.badRequest("MIN_SCORE_REQUIRED", "MIN_SCORE needs minScore (points)");
                if (x.getPointsPossible() != null && r.minScore().compareTo(x.getPointsPossible()) > 0) {
                    throw ApiException.badRequest("MIN_SCORE_TOO_HIGH", "minScore is higher than the assignment's points");
                }
            }
            req = requirements.findByModuleIdAndAssignmentId(moduleId, x.getAssignmentId()).orElseGet(ModuleCompletionRequirement::new);
            req.setAssignmentId(x.getAssignmentId());
            req.setMinScore(r.requirementType() == ModuleCompletionRequirementType.MIN_SCORE ? r.minScore() : null);
            order = x.getDisplayOrder();
        }
        req.setModuleId(moduleId);
        req.setRequirementType(r.requirementType());
        req.setDisplayOrder(order);
        requirements.saveAndFlush(req);
        return modulesService.dto(moduleId);
    }
    @Transactional
    @Override
    public CourseModuleDto remove(UUID moduleId, UUID requirementId) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        blueprintLocks.requireEditable(m.getCourseId(), BlueprintContentLockObjectType.MODULE, moduleId, java.util.EnumSet.of(SETTINGS));
        ModuleCompletionRequirement req = requirements.findById(requirementId).filter(x -> x.getModuleId().equals(moduleId))
                .orElseThrow(() -> ApiException.notFound("Requirement"));
        requirements.delete(req);
        requirements.flush();
        return modulesService.dto(moduleId);
    }

    private CourseModule module(UUID id) {
        return modules.findById(id).orElseThrow(() -> ApiException.notFound("Module"));
    }
}
