package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.BlueprintContentLockDto;
import learning_management_system.backend.dto.SetBlueprintLockRequest;
import learning_management_system.backend.entity.BlueprintAssociation;
import learning_management_system.backend.entity.BlueprintContentLock;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.enums.BlueprintContentLockObjectType;
import learning_management_system.backend.enums.BlueprintLockAspect;
import learning_management_system.backend.enums.BlueprintSyncRunStatus;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.utility.BlueprintCopyMap;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BlueprintContentLockServiceImplementation implements BlueprintContentLockService {
    static final Set<BlueprintContentLockObjectType> SUPPORTED = EnumSet.of(BlueprintContentLockObjectType.CONTENT,
            BlueprintContentLockObjectType.ASSIGNMENT, BlueprintContentLockObjectType.MODULE, BlueprintContentLockObjectType.SETTINGS);
    static final Set<BlueprintSyncRunStatus> FINISHED = EnumSet.of(BlueprintSyncRunStatus.COMPLETED, BlueprintSyncRunStatus.COMPLETED_WITH_EXCEPTIONS);

    private final BlueprintContentLockRepository locks;
    private final BlueprintAssociationRepository associations;
    private final BlueprintSyncRunRepository runs;
    private final CourseRepository courses;
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final CourseModuleRepository modules;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public BlueprintContentLockServiceImplementation(BlueprintContentLockRepository locks, BlueprintAssociationRepository associations,
                                                     BlueprintSyncRunRepository runs, CourseRepository courses, ContentRepository contents,
                                                     AssignmentRepository assignments, CourseModuleRepository modules, CourseAccess access,
                                                     EntityRefresher refresher) {
        this.locks = locks;
        this.associations = associations;
        this.runs = runs;
        this.courses = courses;
        this.contents = contents;
        this.assignments = assignments;
        this.modules = modules;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BlueprintContentLockDto> list(UUID blueprintCourseId) {
        blueprint(blueprintCourseId);
        access.requireCourseEditor(blueprintCourseId);
        return locks.findByBlueprintCourseIdOrderByDateCreatedAsc(blueprintCourseId).stream().map(l -> toDto(l, null)).toList();
    }

    @Transactional
    @Override
    public BlueprintContentLockDto set(UUID blueprintCourseId, SetBlueprintLockRequest r) {
        blueprint(blueprintCourseId);
        access.requireCourseEditor(blueprintCourseId);
        if (!SUPPORTED.contains(r.objectType())) {
            throw ApiException.badRequest("LOCK_TYPE_NOT_SUPPORTED", r.objectType() + " items cannot be locked yet");
        }
        UUID objectId = r.objectType() == BlueprintContentLockObjectType.SETTINGS ? blueprintCourseId : r.objectId();
        if (objectId == null) throw ApiException.badRequest("OBJECT_REQUIRED", "objectId is required");
        boolean inBlueprint = switch (r.objectType()) {
            case CONTENT -> contents.findById(objectId).filter(c -> c.getCourseId().equals(blueprintCourseId)).isPresent();
            case ASSIGNMENT -> assignments.findById(objectId).filter(a -> a.getCourseId().equals(blueprintCourseId)).isPresent();
            case MODULE -> modules.findById(objectId).filter(m -> m.getCourseId().equals(blueprintCourseId)).isPresent();
            default -> true;
        };
        if (!inBlueprint) throw ApiException.badRequest("NOT_IN_BLUEPRINT", "The item is not part of this blueprint course");
        boolean content = r.lockContent() == null || r.lockContent();
        boolean points = Boolean.TRUE.equals(r.lockPoints());
        boolean due = Boolean.TRUE.equals(r.lockDueDates());
        boolean availability = Boolean.TRUE.equals(r.lockAvailabilityDates());
        boolean settings = Boolean.TRUE.equals(r.lockSettings());
        Optional<BlueprintContentLock> existing = locks.findByBlueprintCourseIdAndObjectTypeAndObjectId(blueprintCourseId, r.objectType(), objectId);
        if (!content && !points && !due && !availability && !settings) {
            existing.ifPresent(locks::delete);
            return null;
        }
        BlueprintContentLock l = existing.orElseGet(() -> {
            BlueprintContentLock n = new BlueprintContentLock();
            n.setBlueprintCourseId(blueprintCourseId);
            n.setObjectType(r.objectType());
            n.setObjectId(objectId);
            return n;
        });
        l.setLockContent(content);
        l.setLockPoints(points);
        l.setLockDueDates(due);
        l.setLockAvailabilityDates(availability);
        l.setLockSettings(settings);
        return toDto(refresher.refresh(locks.saveAndFlush(l)), null);
    }

    @Transactional
    @Override
    public void remove(UUID blueprintCourseId, UUID lockId) {
        access.requireCourseEditor(blueprintCourseId);
        locks.delete(locks.findByLockIdAndBlueprintCourseId(lockId, blueprintCourseId).orElseThrow(() -> ApiException.notFound("Lock")));
    }

    @Transactional(readOnly = true)
    @Override
    public List<BlueprintContentLockDto> locksInCourse(UUID courseId) {
        BlueprintAssociation a = associations.findFirstByAssociatedCourseIdAndIsActiveTrue(courseId).orElse(null);
        if (a == null) return List.of();
        Map<String, UUID> map = copyMap(a.getBlueprintCourseId(), courseId);
        List<BlueprintContentLockDto> out = new ArrayList<>();
        for (BlueprintContentLock l : locks.findByBlueprintCourseIdOrderByDateCreatedAsc(a.getBlueprintCourseId())) {
            UUID copy = l.getObjectType() == BlueprintContentLockObjectType.SETTINGS ? courseId
                    : map.get(BlueprintCopyMap.key(l.getObjectType().name(), l.getObjectId()));
            if (copy != null) out.add(toDto(l, copy));
        }
        return out;
    }

    @Override
    public void requireEditable(UUID courseId, BlueprintContentLockObjectType type, UUID itemId, Set<BlueprintLockAspect> aspects) {
        if (aspects.isEmpty()) return;
        BlueprintAssociation a = associations.findFirstByAssociatedCourseIdAndIsActiveTrue(courseId).orElse(null);
        if (a == null) return;
        UUID bpItem = type == BlueprintContentLockObjectType.SETTINGS ? a.getBlueprintCourseId()
                : BlueprintCopyMap.blueprintItemFor(copyMap(a.getBlueprintCourseId(), courseId), type.name(), itemId);
        if (bpItem == null) return;                                      // the course's own item
        BlueprintContentLock l = locks.findByBlueprintCourseIdAndObjectTypeAndObjectId(a.getBlueprintCourseId(), type, bpItem).orElse(null);
        if (l == null) return;
        List<String> hit = new ArrayList<>();
        if (type == BlueprintContentLockObjectType.SETTINGS) hit.add("course settings");      // a settings lock covers them all
        else for (BlueprintLockAspect x : aspects) if (covers(l, x)) hit.add(x.name().toLowerCase(Locale.ROOT).replace('_', ' '));
        if (!hit.isEmpty()) {
            throw ApiException.conflict("BLUEPRINT_LOCKED", "Locked by the blueprint course: " + String.join(", ", hit));
        }
    }

    static boolean covers(BlueprintContentLock l, BlueprintLockAspect a) {
        return switch (a) {
            case CONTENT -> Boolean.TRUE.equals(l.getLockContent());
            case POINTS -> Boolean.TRUE.equals(l.getLockPoints());
            case DUE_DATES -> Boolean.TRUE.equals(l.getLockDueDates());
            case AVAILABILITY -> Boolean.TRUE.equals(l.getLockAvailabilityDates());
            case SETTINGS -> Boolean.TRUE.equals(l.getLockSettings());
        };
    }

    private Map<String, UUID> copyMap(UUID blueprintCourseId, UUID courseId) {
        return runs.findFirstByBlueprintCourseIdAndRunStatusInOrderByDateCreatedDesc(blueprintCourseId, FINISHED)
                .map(r -> BlueprintCopyMap.read(r.getChanges()).getOrDefault(courseId, Map.of())).orElse(Map.of());
    }

    private Course blueprint(UUID id) {
        Course c = courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
        if (c.getCourseKind() != CourseKind.BLUEPRINT) throw ApiException.badRequest("NOT_A_BLUEPRINT", "The course is not a blueprint course");
        return c;
    }

    private BlueprintContentLockDto toDto(BlueprintContentLock l, UUID copyId) {
        return new BlueprintContentLockDto(l.getLockId(), l.getBlueprintCourseId(), l.getObjectType(), l.getObjectId(), copyId,
                l.getLockContent(), l.getLockPoints(), l.getLockDueDates(), l.getLockAvailabilityDates(), l.getLockSettings(), l.getDateCreated());
    }
}
