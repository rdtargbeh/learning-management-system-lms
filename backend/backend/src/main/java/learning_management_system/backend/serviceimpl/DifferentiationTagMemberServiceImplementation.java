package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddTagMembersRequest;
import learning_management_system.backend.dto.DifferentiationTagMemberDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.DifferentiationTag;
import learning_management_system.backend.entity.DifferentiationTagMember;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.DifferentiationTagMemberRepository;
import learning_management_system.backend.repository.DifferentiationTagRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.DifferentiationTagMemberService;
import learning_management_system.backend.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DifferentiationTagMemberServiceImplementation implements DifferentiationTagMemberService {
    private final DifferentiationTagMemberRepository tagMembers;
    private final DifferentiationTagRepository tags;
    private final EnrollmentRepository enrollments;
    private final UserService userService;
    private final CourseAccess access;

    public DifferentiationTagMemberServiceImplementation(DifferentiationTagMemberRepository tagMembers, DifferentiationTagRepository tags,
                                                         EnrollmentRepository enrollments, UserService userService, CourseAccess access) {
        this.tagMembers = tagMembers;
        this.tags = tags;
        this.enrollments = enrollments;
        this.userService = userService;
        this.access = access;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DifferentiationTagMemberDto> list(UUID tagId) {
        DifferentiationTag t = tag(tagId);
        Access.requireTenant();
        if (!access.canSeeEverything() && !access.isCourseStaff(t.getCourseId())) throw ApiException.forbidden("Only course staff can see tags");
        return toDtos(tagMembers.findByTagIdOrderByDateCreatedAsc(tagId));
    }

    @Transactional
    @Override
    public List<DifferentiationTagMemberDto> add(UUID tagId, AddTagMembersRequest r) {
        DifferentiationTag t = tag(tagId);
        access.requireCourseEditor(t.getCourseId());
        for (UUID userId : new LinkedHashSet<>(r.userIds())) {
            boolean student = enrollments.activeInCourse(t.getCourseId(), userId).stream()
                    .map(Enrollment::getEnrollmentType).anyMatch(EnrollmentProvisioningServiceImplementation.SEAT_TYPES::contains);
            if (!student) throw ApiException.badRequest("NOT_A_STUDENT", "User " + userId + " is not a student of this course");
            if (tagMembers.existsByTagIdAndUserId(tagId, userId)) continue;
            DifferentiationTagMember m = new DifferentiationTagMember();
            m.setTagId(tagId);
            m.setUserId(userId);
            tagMembers.save(m);
        }
        tagMembers.flush();
        return toDtos(tagMembers.findByTagIdOrderByDateCreatedAsc(tagId));
    }

    @Transactional
    @Override
    public void remove(UUID tagId, UUID userId) {
        DifferentiationTag t = tag(tagId);
        access.requireCourseEditor(t.getCourseId());
        tagMembers.delete(tagMembers.findByTagIdAndUserId(tagId, userId).orElseThrow(() -> ApiException.notFound("Tagged student")));
    }

    @Override
    public boolean isTagged(UUID tagId, UUID userId) {
        return tagMembers.existsByTagIdAndUserId(tagId, userId);
    }

    private List<DifferentiationTagMemberDto> toDtos(List<DifferentiationTagMember> list) {
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(DifferentiationTagMember::getUserId).toList());
        return list.stream().map(m -> {
            UserSummaryDto u = names.get(m.getUserId());
            return new DifferentiationTagMemberDto(m.getTagMemberId(), m.getTagId(), m.getUserId(), u == null ? null : u.username(),
                    u == null ? null : u.displayName(), m.getDateCreated());
        }).toList();
    }

    private DifferentiationTag tag(UUID id) {
        return tags.findById(id).orElseThrow(() -> ApiException.notFound("Tag"));
    }
}
