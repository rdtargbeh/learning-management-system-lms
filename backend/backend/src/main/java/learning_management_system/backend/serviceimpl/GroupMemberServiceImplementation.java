package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.MyGroupDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.GroupMemberRole;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.GroupMapper;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.GroupLeaderService;
import learning_management_system.backend.service.GroupMemberService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupMemberServiceImplementation implements GroupMemberService {
    private final GroupMemberRepository members;
    private final StudentGroupRepository groups;
    private final GroupSetRepository groupSets;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final GroupLeaderService leaders;
    private final UserService userService;
    private final CourseAccess access;
    private final GroupMapper groupMapper;
    private final EntityRefresher refresher;

    public GroupMemberServiceImplementation(GroupMemberRepository members, StudentGroupRepository groups, GroupSetRepository groupSets,
                                            CourseRepository courses, CourseSectionRepository sections, EnrollmentRepository enrollments,
                                            GroupLeaderService leaders, UserService userService, CourseAccess access, GroupMapper groupMapper,
                                            EntityRefresher refresher) {
        this.members = members;
        this.groups = groups;
        this.groupSets = groupSets;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.leaders = leaders;
        this.userService = userService;
        this.access = access;
        this.groupMapper = groupMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupMemberDto> list(UUID groupId) {
        StudentGroup g = groups.findById(groupId).orElseThrow(() -> ApiException.notFound("Group"));
        access.requireCourseViewer(course(set(g.getGroupSetId()).getCourseId()));
        List<GroupMember> list = members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(groupId);
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(GroupMember::getUserId).toList());
        return list.stream().map(m -> toDto(m, names.get(m.getUserId()))).toList();
    }

    @Transactional
    @Override
    public GroupMemberDto add(UUID groupId, AddGroupMemberRequest r) {
        Access.requireTenant();
        StudentGroup g = lockGroup(groupId);
        GroupSet gs = set(g.getGroupSetId());
        UUID userId = r == null || r.userId() == null ? Access.ctx().userId() : r.userId();
        boolean self = Access.isSelf(userId);
        if (self && !Boolean.TRUE.equals(gs.getAllowSelfSignup())) {
            if (!access.isCourseTeacher(gs.getCourseId())) throw ApiException.forbidden("This group set does not allow self sign-up");
        } else if (!self) {
            access.requireCourseEditor(gs.getCourseId());
        }
        join(g, gs, userId);
        GroupMember m = members.findByGroupIdAndUserId(groupId, userId).orElseThrow();
        if (r != null && r.role() != null && !self && r.role() != GroupMemberRole.LEADER && m.getRole() != GroupMemberRole.LEADER) {
            m.setRole(r.role());                                // LEADER comes from group_leader (appoint a leader instead)
            members.saveAndFlush(m);
        }
        return toDto(m, userService.summariesById(List.of(userId)).get(userId));
    }

    @Override
    public void join(StudentGroup g, GroupSet gs, UUID userId) {
        if (Boolean.FALSE.equals(g.getIsActive())) throw ApiException.conflict("GROUP_INACTIVE", "The group is not active");
        if (!isEligible(gs, g.getSectionId(), userId)) {
            throw ApiException.badRequest("NOT_ENROLLED", "Only students enrolled in the course can be in its groups");
        }
        GroupMember existingInSet = members.activeInSet(gs.getGroupSetId(), userId).orElse(null);
        if (existingInSet != null) {
            throw ApiException.conflict("ALREADY_IN_GROUP", existingInSet.getGroupId().equals(g.getGroupId())
                    ? "Already a member of this group" : "Already in another group of this set");
        }
        long count = members.countByGroupIdAndIsActiveTrue(g.getGroupId());
        Integer limit = g.getMaxMembers() != null ? g.getMaxMembers() : gs.getMaxMembersPerGroup();
        if (limit != null && count >= limit) throw ApiException.conflict("GROUP_FULL", "The group is full");
        GroupMember m = members.findByGroupIdAndUserId(g.getGroupId(), userId).orElseGet(GroupMember::new);
        m.setGroupId(g.getGroupId());
        m.setUserId(userId);
        m.setRole(GroupMemberRole.MEMBER);
        m.setIsActive(true);
        m.setJoinedAt(OffsetDateTime.now());
        m.setLeftAt(null);
        refresher.refresh(members.saveAndFlush(m));
        leaders.memberJoined(g.getGroupId(), userId, Boolean.TRUE.equals(gs.getAutoAssignLeader()));
        recount(g);
    }

    @Transactional
    @Override
    public void remove(UUID groupId, UUID userId) {
        Access.requireTenant();
        StudentGroup g = lockGroup(groupId);
        GroupSet gs = set(g.getGroupSetId());
        if (!(Access.isSelf(userId) && Boolean.TRUE.equals(gs.getAllowSelfSignup()))) access.requireCourseEditor(gs.getCourseId());
        GroupMember m = members.findByGroupIdAndUserId(groupId, userId).filter(x -> Boolean.TRUE.equals(x.getIsActive()))
                .orElseThrow(() -> ApiException.notFound("Group member"));
        m.setIsActive(false);
        m.setLeftAt(OffsetDateTime.now());
        m.setRole(GroupMemberRole.MEMBER);
        members.saveAndFlush(m);
        leaders.memberLeft(groupId, userId, Boolean.TRUE.equals(gs.getAutoAssignLeader()));
        recount(g);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MyGroupDto> mine(UUID courseId) {
        access.requireCourseViewer(course(courseId));
        return members.activeInCourse(courseId, Access.ctx().userId()).stream().map(m -> {
            StudentGroup g = groups.findById(m.getGroupId()).orElseThrow();
            GroupSet gs = set(g.getGroupSetId());
            return new MyGroupDto(g.getGroupId(), g.getGroupName(), gs.getGroupSetId(), gs.getSetName(), m.getRole(), g.getCurrentMembers());
        }).toList();
    }

    @Override
    public boolean isEligible(GroupSet gs, UUID sectionId, UUID userId) {
        if (Boolean.TRUE.equals(gs.getRestrictToSection())) {
            return enrollments.findBySectionIdAndUserId(sectionId, userId).filter(this::activeStudent).isPresent();
        }
        return sections.findByCourseIdOrderBySectionNumberAsc(gs.getCourseId()).stream()
                .anyMatch(s -> enrollments.findBySectionIdAndUserId(s.getSectionId(), userId).filter(this::activeStudent).isPresent());
    }

    // ---------------------------------------------------------------- helpers

    private boolean activeStudent(Enrollment e) {
        return e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE
                && EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType());
    }

    private void recount(StudentGroup g) {
        g.setCurrentMembers((int) members.countByGroupIdAndIsActiveTrue(g.getGroupId()));
        groups.saveAndFlush(g);
    }

    private GroupMemberDto toDto(GroupMember m, UserSummaryDto u) {
        return groupMapper.toDto(m, u == null ? null : u.username(), u == null ? null : u.displayName());
    }

    private StudentGroup lockGroup(UUID id) {
        return groups.lockById(id).orElseThrow(() -> ApiException.notFound("Group"));
    }

    private GroupSet set(UUID id) {
        return groupSets.findById(id).orElseThrow(() -> ApiException.notFound("Group set"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
