package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.service.ItemAvailabilityService;
import learning_management_system.backend.service.ContentPermissionService;
import learning_management_system.backend.service.VideoLectureService;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.BlueprintLockAspect;
import static learning_management_system.backend.enums.BlueprintLockAspect.*;
import learning_management_system.backend.dto.ContentDto;
import learning_management_system.backend.dto.ContentProgressDto;
import learning_management_system.backend.dto.CreateContentRequest;
import learning_management_system.backend.dto.MoveContentRequest;
import learning_management_system.backend.dto.UpdateContentRequest;
import learning_management_system.backend.dto.VideoLectureDto;
import learning_management_system.backend.entity.Content;
import learning_management_system.backend.entity.ContentType;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseModule;
import learning_management_system.backend.entity.StudentContentProgress;
import learning_management_system.backend.entity.VideoLecture;
import learning_management_system.backend.enums.EditingRoles;
import learning_management_system.backend.enums.StudentContentProgressStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.ContentMapper;
import learning_management_system.backend.repository.AssignmentRepository;
import learning_management_system.backend.repository.ContentRepository;
import learning_management_system.backend.repository.ContentTypeRepository;
import learning_management_system.backend.repository.CourseModuleRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.ModuleCompletionRequirementRepository;
import learning_management_system.backend.repository.StudentContentProgressRepository;
import learning_management_system.backend.repository.VideoLectureRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.ContentService;
import learning_management_system.backend.service.ContentTypeService;
import learning_management_system.backend.service.StudentModuleProgressService;
import learning_management_system.backend.service.StudentModuleProgressService.ModuleState;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ContentServiceImplementation implements ContentService {
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final ContentTypeRepository types;
    private final VideoLectureRepository videos;
    private final CourseRepository courses;
    private final CourseModuleRepository modules;
    private final ModuleCompletionRequirementRepository requirements;
    private final StudentContentProgressRepository contentProgress;
    private final ContentTypeService typeService;
    private final StudentModuleProgressService progress;
    private final CourseAccess access;
    private final ContentMapper contentMapper;
    private final EntityRefresher refresher;

    private final BlueprintContentLockService blueprintLocks;

    private final VideoLectureService videoService;

    private final ContentPermissionService permissions;

    private final ItemAvailabilityService availability;

    public ContentServiceImplementation(ContentRepository contents, AssignmentRepository assignments,
                                        ContentTypeRepository types, VideoLectureRepository videos,
                                        CourseRepository courses, CourseModuleRepository modules,
                                        ModuleCompletionRequirementRepository requirements,
                                        StudentContentProgressRepository contentProgress, ContentTypeService typeService,
                                        StudentModuleProgressService progress, CourseAccess access, ContentMapper contentMapper,
                                        EntityRefresher refresher,
            BlueprintContentLockService blueprintLocks,
            VideoLectureService videoService,
            ContentPermissionService permissions,
            ItemAvailabilityService availability) {
        this.availability = availability;
        this.permissions = permissions;
        this.videoService = videoService;
        this.blueprintLocks = blueprintLocks;
        this.contents = contents;
        this.assignments = assignments;
        this.types = types;
        this.videos = videos;
        this.courses = courses;
        this.modules = modules;
        this.requirements = requirements;
        this.contentProgress = contentProgress;
        this.typeService = typeService;
        this.progress = progress;
        this.access = access;
        this.contentMapper = contentMapper;
        this.refresher = refresher;
    }

    // ---------------------------------------------------------------- reading

    @Transactional
    @Override
    public List<ContentDto> list(UUID courseId, String typeCode) {
        Course c = course(courseId);
        access.requireCourseViewer(c);
        UUID typeId = typeCode == null ? null : types.findByTypeCodeIgnoreCase(typeCode.trim()).map(ContentType::getTypeId).orElse(null);
        if (typeCode != null && typeId == null) return List.of();
        List<Content> all = contents.findByCourseIdOrderByDisplayOrderAscContentTitleAsc(courseId).stream()
                .filter(x -> typeId == null || typeId.equals(x.getTypeId())).toList();
        if (isStaff(courseId)) return all.stream().map(this::fullDto).toList();

        Map<UUID, ModuleState> states = progress.evaluate(courseId, Access.ctx().userId());
        List<ContentDto> out = new ArrayList<>();
        for (Content x : all) {
            if (!visibleToStudent(x, states)) continue;
            String reason = lockReason(x, states);
            out.add(reason == null ? fullDto(x) : contentMapper.toLockedDto(x, typeCode(x), reason));
        }
        return out;
    }

    @Transactional
    @Override
    public ContentDto frontPage(UUID courseId) {
        Content x = contents.findFirstByCourseIdAndIsFrontPageTrue(courseId).orElseThrow(() -> ApiException.notFound("Front page"));
        return get(x.getContentId());
    }

    @Transactional
    @Override
    public ContentDto get(UUID contentId) {
        Content x = load(contentId);
        Course c = course(x.getCourseId());
        access.requireCourseViewer(c);
        if (isStaff(c.getCourseId())) return fullDto(x);
        UUID me = Access.ctx().userId();
        Map<UUID, ModuleState> states = progress.evaluate(c.getCourseId(), me);
        if (!visibleToStudent(x, states)) throw ApiException.notFound("Content");
        String reason = lockReason(x, states);
        if (reason != null) return contentMapper.toLockedDto(x, typeCode(x), reason);
        if (progress.isTrackedStudent(c.getCourseId(), me)) progress.recordView(x, me);
        return fullDto(x);
    }

    // ---------------------------------------------------------------- authoring

    @Transactional
    @Override
    public ContentDto create(UUID courseId, CreateContentRequest r) {
        Course c = course(courseId);
        access.requireCourseEditor(courseId);
        if (Boolean.TRUE.equals(c.getIsArchived())) throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
        ContentType type = typeService.resolve(r.typeCode());
        validateDates(r.dateStart(), r.dateEnd());
        Content x = contentMapper.toEntity(r);
        x.setCourseId(courseId);
        x.setTypeId(type.getTypeId());
        x.setContentTitle(r.contentTitle().trim());
        x.setIsPublished(false);
        if (r.moduleId() != null) {
            module(r.moduleId(), courseId);
            x.setDisplayOrder(nextOrder(r.moduleId()));
        }
        refresher.refresh(contents.saveAndFlush(x));
        if (r.video() != null) videoService.saveFor(x, r.video());
        else if ("VIDEO".equals(type.getTypeCode())) throw ApiException.badRequest("VIDEO_REQUIRED", "A VIDEO item needs video details");
        if (Boolean.TRUE.equals(r.isFrontPage())) makeFrontPage(x);
        return fullDto(load(x.getContentId()));
    }

    @Transactional
    @Override
    public ContentDto update(UUID contentId, UpdateContentRequest r) {
        Content x = load(contentId);
        Course c = course(x.getCourseId());
        boolean editor = Access.hasTenantPermission(Permissions.COURSE_MANAGE)
                || access.isCourseTeacher(c.getCourseId());
        if (!editor) requireStudentEditable(x, c, r);
        java.util.EnumSet<BlueprintLockAspect> touched = java.util.EnumSet.noneOf(BlueprintLockAspect.class);
        if (r.contentTitle() != null || r.contentDescription() != null || r.contentBody() != null || r.contentHtml() != null || r.video() != null) touched.add(CONTENT);
        if (r.dateStart() != null || r.dateEnd() != null) touched.add(AVAILABILITY);
        if (r.estimatedDurationMinutes() != null || r.accessLevel() != null || r.editingRoles() != null || r.isLocked() != null || r.isFrontPage() != null) touched.add(SETTINGS);
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, touched);
        validateDates(r.dateStart() != null ? r.dateStart() : x.getDateStart(), r.dateEnd() != null ? r.dateEnd() : x.getDateEnd());
        contentMapper.updateEntity(r, x);
        contents.saveAndFlush(x);
        if (r.video() != null) videoService.saveFor(x, r.video());
        if (Boolean.TRUE.equals(r.isFrontPage())) makeFrontPage(x);
        else if (Boolean.FALSE.equals(r.isFrontPage()) && Boolean.TRUE.equals(x.getIsFrontPage())) {
            x.setIsFrontPage(false);
            contents.saveAndFlush(x);
        }
        return fullDto(load(contentId));
    }

    /** Students may edit only the text of a published page they can open, and only if its editingRoles allow it. */
    private void requireStudentEditable(Content x, Course c, UpdateContentRequest r) {
        access.requireCourseViewer(c);
        boolean allowed = x.getEditingRoles() == EditingRoles.TEACHERS_STUDENTS || x.getEditingRoles() == EditingRoles.ANYONE;
        boolean textOnly = r.estimatedDurationMinutes() == null && r.accessLevel() == null && r.editingRoles() == null && r.dateStart() == null
                && r.dateEnd() == null && r.isLocked() == null && r.isFrontPage() == null && r.video() == null;
        Map<UUID, ModuleState> states = progress.evaluate(c.getCourseId(), Access.ctx().userId());
        if (!allowed || !textOnly || !visibleToStudent(x, states) || lockReason(x, states) != null || Boolean.TRUE.equals(x.getIsLocked())) {
            throw ApiException.forbidden("You cannot edit this item");
        }
    }

    @Transactional
    @Override
    public ContentDto setPublished(UUID contentId, boolean published) {
        Content x = load(contentId);
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, java.util.EnumSet.of(AVAILABILITY));
        if (!published && Boolean.TRUE.equals(x.getIsFrontPage())) {
            throw ApiException.conflict("FRONT_PAGE", "The front page cannot be unpublished; choose another front page first");
        }
        x.setIsPublished(published);
        if (published && x.getDatePublished() == null) x.setDatePublished(OffsetDateTime.now());
        return fullDto(contents.saveAndFlush(x));
    }

    @Transactional
    @Override
    public ContentDto move(UUID contentId, MoveContentRequest r) {
        Content x = load(contentId);
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, java.util.EnumSet.of(SETTINGS));
        if (r.moduleId() == null) {
            requirements.deleteByContentId(contentId);
            x.setModuleId(null);
            x.setDisplayOrder(0);
        } else {
            module(r.moduleId(), x.getCourseId());
            if (!r.moduleId().equals(x.getModuleId())) {
                requirements.deleteForContentOutsideModule(contentId, r.moduleId());
                x.setModuleId(r.moduleId());
                x.setDisplayOrder(nextOrder(r.moduleId()));
            }
        }
        return fullDto(contents.saveAndFlush(x));
    }

    @Transactional
    @Override
    public void delete(UUID contentId) {
        Content x = load(contentId);
        access.requireCourseEditor(x.getCourseId());
        blueprintLocks.requireEditable(x.getCourseId(), BlueprintContentLockObjectType.CONTENT, contentId, java.util.EnumSet.of(CONTENT));
        if (Boolean.TRUE.equals(x.getIsFrontPage())) {
            throw ApiException.conflict("FRONT_PAGE", "The front page cannot be deleted; choose another front page first");
        }
        contents.delete(x);              // video, blocks, requirements and progress rows go with it
        contents.flush();
    }

    // ---------------------------------------------------------------- student progress

    @Transactional
    @Override
    public ContentProgressDto markDone(UUID contentId, boolean done) {
        Content x = load(contentId);
        Course c = course(x.getCourseId());
        access.requireCourseViewer(c);
        UUID me = Access.ctx().userId();
        if (!progress.isTrackedStudent(c.getCourseId(), me)) throw ApiException.forbidden("Only students can mark items as done");
        Map<UUID, ModuleState> states = progress.evaluate(c.getCourseId(), me);
        if (!visibleToStudent(x, states)) throw ApiException.notFound("Content");
        String reason = lockReason(x, states);
        if (reason != null) throw ApiException.conflict("ITEM_LOCKED", reason);
        return contentMapper.toDto(progress.markDone(x, me, done));
    }

    @Transactional(readOnly = true)
    @Override
    public ContentProgressDto myProgress(UUID contentId) {
        Content x = load(contentId);
        access.requireCourseViewer(course(x.getCourseId()));
        UUID me = Access.ctx().userId();
        return contentProgress.findByUserIdAndContentId(me, contentId).map(contentMapper::toDto)
                .orElse(new ContentProgressDto(contentId, StudentContentProgressStatus.NOT_STARTED, null, 0, null, null, null));
    }

    // ---------------------------------------------------------------- helpers

    /** Published, viewable under the item's permissions (RESTRICTED needs a grant), and (if in a module) in a published module the student can see. */
    private boolean visibleToStudent(Content x, Map<UUID, ModuleState> states) {
        if (!Boolean.TRUE.equals(x.getIsPublished()) || !permissions.canView(x, Access.ctx().userId())) return false;
        return x.getModuleId() == null || states.containsKey(x.getModuleId());
    }

    private String lockReason(Content x, Map<UUID, ModuleState> states) {
        LocalDate today = LocalDate.now();
        if (x.getDateStart() != null && today.isBefore(x.getDateStart())) return "Available from " + x.getDateStart();
        if (x.getDateEnd() != null && today.isAfter(x.getDateEnd())) return "Closed on " + x.getDateEnd();
        if (x.getModuleId() != null) {
            ModuleState s = states.get(x.getModuleId());
            if (s == null) return "Not available";
            String locked = s.lockedItems().get(x.getContentId());
            if (locked != null) return locked;
        }
        // release conditions and mastery paths
        return availability.lockReason(x.getCourseId(), learning_management_system.backend.enums.TargetType.CONTENT, x.getContentId(),
                x.getModuleId(), Access.ctx().userId());
    }

    private void makeFrontPage(Content x) {
        if (!Boolean.TRUE.equals(x.getIsPublished())) {
            x.setIsPublished(true);              // the front page is always visible (Canvas does the same)
            if (x.getDatePublished() == null) x.setDatePublished(OffsetDateTime.now());
        }
        contents.clearFrontPageExcept(x.getCourseId(), x.getContentId());
        Content fresh = load(x.getContentId());
        fresh.setIsFrontPage(true);
        fresh.setIsPublished(true);
        if (fresh.getDatePublished() == null) fresh.setDatePublished(OffsetDateTime.now());
        contents.saveAndFlush(fresh);
    }


    /** Content and assignments share one order inside a module: new items go after both. */
    private int nextOrder(UUID moduleId) {
        return Math.max(contents.maxDisplayOrderInModule(moduleId), assignments.maxDisplayOrderInModule(moduleId)) + 1;
    }

    private ContentDto fullDto(Content x) {
        VideoLectureDto video = videos.findFirstByContentId(x.getContentId()).map(contentMapper::toDto).orElse(null);
        return contentMapper.toDto(x, typeCode(x), video);
    }

    private String typeCode(Content x) {
        return types.findById(x.getTypeId()).map(ContentType::getTypeCode).orElse(null);
    }

    private boolean isStaff(UUID courseId) {
        return access.canSeeEverything() || access.isCourseStaff(courseId);
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
    }

    private CourseModule module(UUID moduleId, UUID courseId) {
        return modules.findById(moduleId).filter(m -> m.getCourseId().equals(courseId))
                .orElseThrow(() -> ApiException.badRequest("MODULE_NOT_IN_COURSE", "The module does not belong to this course"));
    }

    private Content load(UUID id) {
        return contents.findById(id).orElseThrow(() -> ApiException.notFound("Content"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
