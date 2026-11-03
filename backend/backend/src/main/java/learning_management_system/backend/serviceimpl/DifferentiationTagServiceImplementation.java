package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.DifferentiationTagDto;
import learning_management_system.backend.dto.DifferentiationTagRequest;
import learning_management_system.backend.entity.DifferentiationTag;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.DifferentiationTagMemberRepository;
import learning_management_system.backend.repository.DifferentiationTagRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.DifferentiationTagService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DifferentiationTagServiceImplementation implements DifferentiationTagService {
    private final DifferentiationTagRepository tags;
    private final DifferentiationTagMemberRepository tagMembers;
    private final CourseRepository courses;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public DifferentiationTagServiceImplementation(DifferentiationTagRepository tags, DifferentiationTagMemberRepository tagMembers,
                                                   CourseRepository courses, CourseAccess access, EntityRefresher refresher) {
        this.tags = tags;
        this.tagMembers = tagMembers;
        this.courses = courses;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DifferentiationTagDto> list(UUID courseId) {
        requireStaff(courseId);
        return tags.findByCourseIdOrderByTagNameAsc(courseId).stream().map(this::toDto).toList();
    }

    @Transactional
    @Override
    public DifferentiationTagDto create(UUID courseId, DifferentiationTagRequest r) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        access.requireCourseEditor(courseId);
        if (r.tagName() == null || r.tagName().isBlank()) throw ApiException.badRequest("NAME_REQUIRED", "tagName is required");
        String name = r.tagName().trim();
        if (tags.existsByCourseIdAndTagNameIgnoreCase(courseId, name)) throw ApiException.conflict("TAG_NAME_TAKEN", "The course already has this tag");
        DifferentiationTag t = new DifferentiationTag();
        t.setCourseId(courseId);
        t.setTagName(name);
        t.setTagDescription(r.tagDescription());
        t.setCreatedBy(Access.ctx().userId());
        return toDto(refresher.refresh(tags.saveAndFlush(t)));
    }

    @Transactional
    @Override
    public DifferentiationTagDto update(UUID tagId, DifferentiationTagRequest r) {
        DifferentiationTag t = load(tagId);
        access.requireCourseEditor(t.getCourseId());
        if (r.tagName() != null) {
            String name = r.tagName().trim();
            if (!name.equalsIgnoreCase(t.getTagName()) && tags.existsByCourseIdAndTagNameIgnoreCase(t.getCourseId(), name)) {
                throw ApiException.conflict("TAG_NAME_TAKEN", "The course already has this tag");
            }
            t.setTagName(name);
        }
        if (r.tagDescription() != null) t.setTagDescription(r.tagDescription());
        return toDto(tags.saveAndFlush(t));
    }

    @Transactional
    @Override
    public void delete(UUID tagId) {
        DifferentiationTag t = load(tagId);
        access.requireCourseEditor(t.getCourseId());
        tags.delete(t);                                   // members and tag overrides go with it
    }

    /** Teachers, TAs, graders and course administrators: tags are never shown to students. */
    private void requireStaff(UUID courseId) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        Access.requireTenant();
        if (!access.canSeeEverything() && !access.isCourseStaff(courseId)) throw ApiException.forbidden("Only course staff can see tags");
    }

    private DifferentiationTag load(UUID id) {
        return tags.findById(id).orElseThrow(() -> ApiException.notFound("Tag"));
    }

    private DifferentiationTagDto toDto(DifferentiationTag t) {
        return new DifferentiationTagDto(t.getTagId(), t.getCourseId(), t.getTagName(), t.getTagDescription(),
                tagMembers.countByTagId(t.getTagId()), t.getCreatedBy(), t.getDateCreated());
    }
}
