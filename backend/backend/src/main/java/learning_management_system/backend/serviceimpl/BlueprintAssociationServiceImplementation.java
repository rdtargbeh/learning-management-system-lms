package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssociateBlueprintCoursesRequest;
import learning_management_system.backend.dto.BlueprintAssociationDto;
import learning_management_system.backend.dto.BlueprintStatusDto;
import learning_management_system.backend.entity.BlueprintAssociation;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.enums.CourseKind;
import learning_management_system.backend.enums.Severity;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.BlueprintAssociationRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.AuditLogService;
import learning_management_system.backend.service.BlueprintAssociationService;
import learning_management_system.backend.service.BlueprintContentLockService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BlueprintAssociationServiceImplementation implements BlueprintAssociationService {
    private final BlueprintAssociationRepository associations;
    private final CourseRepository courses;
    private final BlueprintContentLockService locks;
    private final CourseAccess access;
    private final EntityRefresher refresher;
    private final AuditLogService audit;

    public BlueprintAssociationServiceImplementation(BlueprintAssociationRepository associations, CourseRepository courses,
                                                     BlueprintContentLockService locks, CourseAccess access, EntityRefresher refresher,
                                                     AuditLogService audit) {
        this.associations = associations;
        this.courses = courses;
        this.locks = locks;
        this.access = access;
        this.refresher = refresher;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @Override
    public List<BlueprintAssociationDto> list(UUID blueprintCourseId) {
        blueprint(blueprintCourseId);
        access.requireCourseEditor(blueprintCourseId);
        return toDtos(associations.findByBlueprintCourseIdOrderByDateCreatedAsc(blueprintCourseId));
    }

    @Transactional
    @Override
    public List<BlueprintAssociationDto> associate(UUID blueprintCourseId, AssociateBlueprintCoursesRequest r) {
        access.requireCourseAdmin();
        blueprint(blueprintCourseId);
        List<BlueprintAssociation> touched = new ArrayList<>();
        for (UUID id : new LinkedHashSet<>(r.courseIds())) {
            Course c = courses.findById(id).orElseThrow(() -> ApiException.badRequest("UNKNOWN_COURSE", "Unknown course " + id));
            if (c.getCourseKind() != CourseKind.STANDARD) {
                throw ApiException.badRequest("NOT_A_STANDARD_COURSE", c.getCourseCode() + " is a " + c.getCourseKind() + " course; only standard courses can be associated");
            }
            Optional<BlueprintAssociation> other = associations.findFirstByAssociatedCourseIdAndIsActiveTrue(id);
            if (other.isPresent() && !other.get().getBlueprintCourseId().equals(blueprintCourseId)) {
                throw ApiException.conflict("HAS_BLUEPRINT", c.getCourseCode() + " is already associated with another blueprint");
            }
            BlueprintAssociation a = associations.findByBlueprintCourseIdAndAssociatedCourseId(blueprintCourseId, id).orElseGet(() -> {
                BlueprintAssociation n = new BlueprintAssociation();
                n.setBlueprintCourseId(blueprintCourseId);
                n.setAssociatedCourseId(id);
                return n;
            });
            a.setIsActive(true);
            touched.add(refresher.refresh(associations.saveAndFlush(a)));
            audit.record("BLUEPRINT_ASSOCIATED", "COURSE", id, null, Severity.INFO, null, Map.of("blueprintCourseId", blueprintCourseId.toString()));
        }
        return toDtos(touched);
    }

    @Transactional
    @Override
    public void remove(UUID blueprintCourseId, UUID associationId) {
        access.requireCourseAdmin();
        BlueprintAssociation a = associations.findByBlueprintAssociationIdAndBlueprintCourseId(associationId, blueprintCourseId)
                .orElseThrow(() -> ApiException.notFound("Association"));
        a.setIsActive(false);
        associations.save(a);
        audit.record("BLUEPRINT_UNASSOCIATED", "COURSE", a.getAssociatedCourseId(), null, Severity.INFO, null,
                Map.of("blueprintCourseId", blueprintCourseId.toString()));
    }

    @Transactional(readOnly = true)
    @Override
    public BlueprintStatusDto status(UUID courseId) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        access.requireCourseEditor(courseId);
        BlueprintAssociation a = associations.findFirstByAssociatedCourseIdAndIsActiveTrue(courseId)
                .orElseThrow(() -> ApiException.notFound("Blueprint for this course"));
        Course bp = courses.findById(a.getBlueprintCourseId()).orElseThrow();
        return new BlueprintStatusDto(courseId, bp.getCourseId(), bp.getCourseCode(), bp.getCourseTitle(), a.getDateLastSynced(),
                locks.locksInCourse(courseId));
    }

    private Course blueprint(UUID id) {
        Access.requireTenant();
        Course c = courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
        if (c.getCourseKind() != CourseKind.BLUEPRINT) throw ApiException.badRequest("NOT_A_BLUEPRINT", "The course is not a blueprint course");
        return c;
    }

    private List<BlueprintAssociationDto> toDtos(List<BlueprintAssociation> list) {
        Map<UUID, Course> byId = courses.findAllById(list.stream().map(BlueprintAssociation::getAssociatedCourseId).toList()).stream()
                .collect(Collectors.toMap(Course::getCourseId, Function.identity()));
        return list.stream().map(a -> {
            Course c = byId.get(a.getAssociatedCourseId());
            return new BlueprintAssociationDto(a.getBlueprintAssociationId(), a.getBlueprintCourseId(), a.getAssociatedCourseId(),
                    c == null ? null : c.getCourseCode(), c == null ? null : c.getCourseTitle(), a.getDateLastSynced(), a.getIsActive(),
                    a.getDateCreated());
        }).toList();
    }
}
