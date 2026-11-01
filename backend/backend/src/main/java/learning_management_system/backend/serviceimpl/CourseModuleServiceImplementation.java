package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddModuleRequirementRequest;
import learning_management_system.backend.dto.CourseModuleDto;
import learning_management_system.backend.dto.CreateCourseModuleRequest;
import learning_management_system.backend.dto.ModuleItemDto;
import learning_management_system.backend.dto.ModulePrerequisitesRequest;
import learning_management_system.backend.dto.ModuleRequirementDto;
import learning_management_system.backend.dto.ModuleStudentProgressDto;
import learning_management_system.backend.dto.ModuleViewDto;
import learning_management_system.backend.dto.ReorderRequest;
import learning_management_system.backend.dto.UpdateCourseModuleRequest;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.ContentType;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseModule;
import learning_management_system.backend.entity.ModuleCompletionRequirement;
import learning_management_system.backend.entity.ModulePrerequisite;
import learning_management_system.backend.enums.ModuleCompletionRequirementType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.CourseModuleMapper;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.ContentTypeRepository;
import learning_management_system.backend.repository.CourseModuleRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.ModuleCompletionRequirementRepository;
import learning_management_system.backend.repository.ModulePrerequisiteRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CourseModuleService;
import learning_management_system.backend.service.ModuleProgressService;
import learning_management_system.backend.service.ModuleProgressService.ModuleState;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseModuleServiceImplementation implements CourseModuleService {
    private static final Set<ModuleCompletionRequirementType> CONTENT_REQUIREMENTS =
            EnumSet.of(ModuleCompletionRequirementType.VIEW, ModuleCompletionRequirementType.MARK_DONE);

    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final CourseModuleRepository modules;
    private final ModulePrerequisiteRepository prerequisites;
    private final ModuleCompletionRequirementRepository requirements;
    private final ContentRepository contents;
    private final ContentTypeRepository types;
    private final ModuleProgressService progress;
    private final CourseAccess access;
    private final CourseModuleMapper moduleMapper;
    private final EntityRefresher refresher;

    public CourseModuleServiceImplementation(CourseRepository courses, CourseSectionRepository sections,
                                             CourseModuleRepository modules, ModulePrerequisiteRepository prerequisites,
                                             ModuleCompletionRequirementRepository requirements, ContentRepository contents,
                                             ContentTypeRepository types, ModuleProgressService progress, CourseAccess access,
                                             CourseModuleMapper moduleMapper, EntityRefresher refresher) {
        this.courses = courses;
        this.sections = sections;
        this.modules = modules;
        this.prerequisites = prerequisites;
        this.requirements = requirements;
        this.contents = contents;
        this.types = types;
        this.progress = progress;
        this.access = access;
        this.moduleMapper = moduleMapper;
        this.refresher = refresher;
    }

    // ---------------------------------------------------------------- reading

    @Transactional
    @Override
    public List<ModuleViewDto> list(UUID courseId) {
        Course c = course(courseId);
        access.requireCourseViewer(c);
        List<CourseModule> all = modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(courseId);
        return views(c, all);
    }

    @Transactional
    @Override
    public ModuleViewDto get(UUID moduleId) {
        CourseModule m = module(moduleId);
        Course c = course(m.getCourseId());
        access.requireCourseViewer(c);
        List<ModuleViewDto> v = views(c, List.of(m));
        if (v.isEmpty()) throw ApiException.notFound("Module");
        return v.get(0);
    }

    /** Staff: all modules and items. Students: only what they can see, with their progress and locks. */
    private List<ModuleViewDto> views(Course c, List<CourseModule> list) {
        boolean staff = isStaff(c.getCourseId());
        Map<UUID, ModuleState> states = staff ? Map.of() : progress.evaluate(c.getCourseId(), Access.ctx().userId());
        List<UUID> ids = list.stream().map(CourseModule::getModuleId).toList();
        Map<UUID, List<UUID>> pre = prereqMap(ids);
        Map<UUID, List<ModuleCompletionRequirement>> req = reqMap(ids);
        Map<UUID, List<Content>> items = new HashMap<>();
        if (!ids.isEmpty()) contents.findByModuleIdIn(ids).forEach(x -> items.computeIfAbsent(x.getModuleId(), k -> new ArrayList<>()).add(x));
        Map<UUID, String> typeCodes = types.findAll().stream().collect(Collectors.toMap(ContentType::getTypeId, ContentType::getTypeCode));

        List<ModuleViewDto> out = new ArrayList<>();
        for (CourseModule m : list) {
            ModuleState s = states.get(m.getModuleId());
            if (!staff && s == null) continue;                          // unpublished, or for another section
            Map<UUID, ModuleCompletionRequirementType> reqType = new HashMap<>();
            req.getOrDefault(m.getModuleId(), List.of()).forEach(r -> { if (r.getContentId() != null) reqType.put(r.getContentId(), r.getRequirementType()); });
            List<ModuleItemDto> itemDtos = items.getOrDefault(m.getModuleId(), List.of()).stream()
                    .filter(x -> staff || (Boolean.TRUE.equals(x.getIsPublished()) && ModuleProgressServiceImplementation.studentVisible(x)))
                    .sorted((a, b) -> Integer.compare(nz(a.getDisplayOrder()), nz(b.getDisplayOrder())))
                    .map(x -> new ModuleItemDto(x.getContentId(), x.getContentTitle(), typeCodes.get(x.getTypeId()), x.getIsPublished(),
                            x.getDisplayOrder(), reqType.get(x.getContentId()),
                            staff ? null : s.completedContentIds().contains(x.getContentId()),
                            staff ? null : s.lockedContent().containsKey(x.getContentId())))
                    .toList();
            out.add(new ModuleViewDto(toDto(m, pre, req), staff ? null : s.status(), staff ? null : s.lockReason(), itemDtos));
        }
        return out;
    }

    // ---------------------------------------------------------------- authoring

    @Transactional
    @Override
    public CourseModuleDto create(UUID courseId, CreateCourseModuleRequest r) {
        Course c = course(courseId);
        access.requireCourseEditor(courseId);
        requireNotArchived(c);
        if (r.sectionId() != null) {
            sections.findById(r.sectionId()).filter(s -> s.getCourseId().equals(courseId))
                    .orElseThrow(() -> ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course"));
        }
        validateDates(r.dateStart(), r.dateEnd());
        CourseModule m = moduleMapper.toEntity(r);
        m.setCourseId(courseId);
        m.setModuleTitle(r.moduleTitle().trim());
        int next = modules.maxModuleNumber(courseId) + 1;
        m.setModuleNumber(next);
        m.setDisplayOrder(next);
        m.setIsPublished(false);
        refresher.refresh(modules.saveAndFlush(m));
        return toDto(m);
    }

    @Transactional
    @Override
    public CourseModuleDto update(UUID moduleId, UpdateCourseModuleRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        moduleMapper.updateEntity(r, m);
        validateDates(m.getDateStart(), m.getDateEnd());
        return toDto(modules.saveAndFlush(m));
    }

    @Transactional
    @Override
    public CourseModuleDto setPublished(UUID moduleId, boolean published, boolean includeItems) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        m.setIsPublished(published);
        modules.saveAndFlush(m);
        if (published && includeItems) {
            OffsetDateTime now = OffsetDateTime.now();
            for (Content x : contents.findByModuleIdOrderByDisplayOrderAscContentTitleAsc(moduleId)) {
                if (Boolean.TRUE.equals(x.getIsPublished())) continue;
                x.setIsPublished(true);
                if (x.getDatePublished() == null) x.setDatePublished(now);
                contents.save(x);
            }
            contents.flush();
        }
        return toDto(m);
    }

    @Transactional
    @Override
    public void delete(UUID moduleId) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        modules.delete(m);              // items stay in the course (module_id set to NULL); requirements and progress go
        modules.flush();
    }

    @Transactional
    @Override
    public List<ModuleViewDto> reorder(UUID courseId, ReorderRequest r) {
        Course c = course(courseId);
        access.requireCourseEditor(courseId);
        List<CourseModule> all = modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(courseId);
        requireSameIds(all.stream().map(CourseModule::getModuleId).toList(), r.orderedIds(), "module");
        Map<UUID, CourseModule> byId = all.stream().collect(Collectors.toMap(CourseModule::getModuleId, x -> x));
        for (int i = 0; i < r.orderedIds().size(); i++) byId.get(r.orderedIds().get(i)).setDisplayOrder(i + 1);
        modules.saveAllAndFlush(all);
        return views(c, modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(courseId));
    }

    /** Prerequisites must be earlier modules of the same course (as in Canvas), so there can be no cycles. */
    @Transactional
    @Override
    public CourseModuleDto setPrerequisites(UUID moduleId, ModulePrerequisitesRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
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
        return toDto(m);
    }

    @Transactional
    @Override
    public CourseModuleDto addRequirement(UUID moduleId, AddModuleRequirementRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        if (!CONTENT_REQUIREMENTS.contains(r.requirementType())) {
            throw ApiException.badRequest("REQUIREMENT_NOT_SUPPORTED_YET",
                    r.requirementType() + " requirements arrive with assignments, quizzes and discussions; use VIEW or MARK_DONE");
        }
        Content x = contents.findById(r.contentId()).filter(c -> moduleId.equals(c.getModuleId()))
                .orElseThrow(() -> ApiException.badRequest("ITEM_NOT_IN_MODULE", "The item is not in this module"));
        ModuleCompletionRequirement req = requirements.findByModuleIdAndContentId(moduleId, x.getContentId()).orElseGet(ModuleCompletionRequirement::new);
        req.setModuleId(moduleId);
        req.setContentId(x.getContentId());
        req.setRequirementType(r.requirementType());
        req.setDisplayOrder(x.getDisplayOrder());
        requirements.saveAndFlush(req);
        return toDto(m);
    }

    @Transactional
    @Override
    public CourseModuleDto removeRequirement(UUID moduleId, UUID requirementId) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        ModuleCompletionRequirement req = requirements.findById(requirementId).filter(x -> x.getModuleId().equals(moduleId))
                .orElseThrow(() -> ApiException.notFound("Requirement"));
        requirements.delete(req);
        requirements.flush();
        return toDto(m);
    }

    @Transactional
    @Override
    public ModuleViewDto reorderItems(UUID moduleId, ReorderRequest r) {
        CourseModule m = module(moduleId);
        access.requireCourseEditor(m.getCourseId());
        List<Content> items = contents.findByModuleIdOrderByDisplayOrderAscContentTitleAsc(moduleId);
        requireSameIds(items.stream().map(Content::getContentId).toList(), r.orderedIds(), "item");
        Map<UUID, Content> byId = items.stream().collect(Collectors.toMap(Content::getContentId, x -> x));
        for (int i = 0; i < r.orderedIds().size(); i++) byId.get(r.orderedIds().get(i)).setDisplayOrder(i + 1);
        contents.saveAllAndFlush(items);
        for (ModuleCompletionRequirement req : requirements.findByModuleIdOrderByDisplayOrderAsc(moduleId)) {
            if (req.getContentId() != null && byId.containsKey(req.getContentId())) req.setDisplayOrder(byId.get(req.getContentId()).getDisplayOrder());
        }
        requirements.flush();
        return views(course(m.getCourseId()), List.of(m)).get(0);
    }

    @Transactional
    @Override
    public List<ModuleStudentProgressDto> studentProgress(UUID moduleId) {
        CourseModule m = module(moduleId);
        Access.requireTenant();
        if (!isStaff(m.getCourseId())) throw ApiException.forbidden("Only course staff can see student progress");
        return progress.moduleProgress(moduleId);
    }

    // ---------------------------------------------------------------- helpers

    private boolean isStaff(UUID courseId) {
        return access.canSeeEverything() || access.isCourseStaff(courseId);
    }

    private CourseModuleDto toDto(CourseModule m) {
        List<UUID> one = List.of(m.getModuleId());
        return toDto(m, prereqMap(one), reqMap(one));
    }

    private CourseModuleDto toDto(CourseModule m, Map<UUID, List<UUID>> pre, Map<UUID, List<ModuleCompletionRequirement>> req) {
        List<ModuleRequirementDto> reqs = req.getOrDefault(m.getModuleId(), List.of()).stream().map(moduleMapper::toDto).toList();
        return moduleMapper.toDto(m, pre.getOrDefault(m.getModuleId(), List.of()), reqs);
    }

    private Map<UUID, List<UUID>> prereqMap(List<UUID> ids) {
        Map<UUID, List<UUID>> out = new HashMap<>();
        if (!ids.isEmpty()) prerequisites.findByModuleIdIn(ids).forEach(p -> out.computeIfAbsent(p.getModuleId(), k -> new ArrayList<>()).add(p.getPrerequisiteModuleId()));
        return out;
    }

    private Map<UUID, List<ModuleCompletionRequirement>> reqMap(List<UUID> ids) {
        Map<UUID, List<ModuleCompletionRequirement>> out = new HashMap<>();
        if (!ids.isEmpty()) requirements.findByModuleIdInOrderByDisplayOrderAsc(ids).forEach(r -> out.computeIfAbsent(r.getModuleId(), k -> new ArrayList<>()).add(r));
        return out;
    }

    private static void requireSameIds(List<UUID> current, List<UUID> given, String what) {
        if (given.size() != current.size() || !new HashSet<>(given).equals(new HashSet<>(current))) {
            throw ApiException.badRequest("INVALID_ORDER", "orderedIds must list every " + what + " exactly once");
        }
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
    }

    private static void requireNotArchived(Course c) {
        if (Boolean.TRUE.equals(c.getIsArchived())) throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
    }

    private static int nz(Integer i) { return i == null ? 0 : i; }

    private CourseModule module(UUID id) {
        return modules.findById(id).orElseThrow(() -> ApiException.notFound("Module"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
