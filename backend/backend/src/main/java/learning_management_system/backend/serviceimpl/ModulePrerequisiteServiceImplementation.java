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
import learning_management_system.backend.service.ModulePrerequisiteService;

/** module_prerequisite: modules a student must complete before this one opens. Prerequisites must come earlier. */
@Service
public class ModulePrerequisiteServiceImplementation implements ModulePrerequisiteService {
    private final ModulePrerequisiteRepository prerequisites;
    private final CourseModuleRepository modules;
    private final CourseRepository courses;
    private final CourseModuleService modulesService;
    private final BlueprintContentLockService blueprintLocks;
    private final CourseAccess access;

    public ModulePrerequisiteServiceImplementation(ModulePrerequisiteRepository prerequisites, CourseModuleRepository modules,
                                                   CourseRepository courses, CourseModuleService modulesService,
                                                   BlueprintContentLockService blueprintLocks, CourseAccess access) {
        this.prerequisites = prerequisites;
        this.modules = modules;
        this.courses = courses;
        this.modulesService = modulesService;
        this.blueprintLocks = blueprintLocks;
        this.access = access;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ModulePrerequisiteDto> list(UUID moduleId) {
        CourseModule m = module(moduleId);
        access.requireCourseViewer(courses.findById(m.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        return prerequisites.findByModuleId(moduleId).stream().map(p -> new ModulePrerequisiteDto(p.getModulePrerequisiteId(), moduleId,
                p.getPrerequisiteModuleId(), modules.findById(p.getPrerequisiteModuleId()).map(CourseModule::getModuleTitle).orElse(null))).toList();
    }

    @Transactional
    @Override
    public CourseModuleDto replace(UUID moduleId, ModulePrerequisitesRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        blueprintLocks.requireEditable(m.getCourseId(), BlueprintContentLockObjectType.MODULE, moduleId, java.util.EnumSet.of(SETTINGS));
        List<CourseModule> ordered = modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(m.getCourseId());
        Map<UUID, Integer> position = new HashMap<>();
        for (int i = 0; i < ordered.size(); i++) position.put(ordered.get(i).getModuleId(), i);
        Set<UUID> wanted = new LinkedHashSet<>(r.prerequisiteModuleIds());
        for (UUID p : wanted) {
            Integer pos = position.get(p);
            if (pos == null) throw ApiException.badRequest("MODULE_NOT_IN_COURSE", "Prerequisite " + p + " is not a module of this course");
            if (pos >= position.get(moduleId)) {
                throw ApiException.badRequest("PREREQUISITE_ORDER", "A prerequisite must be a module that comes before this one");
            }
        }
        prerequisites.deleteByModuleId(moduleId);
        for (UUID p : wanted) {
            ModulePrerequisite mp = new ModulePrerequisite();
            mp.setModuleId(moduleId);
            mp.setPrerequisiteModuleId(p);
            prerequisites.save(mp);
        }
        prerequisites.flush();
        return modulesService.dto(moduleId);
    }

    private CourseModule module(UUID id) {
        return modules.findById(id).orElseThrow(() -> ApiException.notFound("Module"));
    }
}
