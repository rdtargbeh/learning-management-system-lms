package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AppointGroupLeaderRequest;
import learning_management_system.backend.dto.GroupLeaderDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.GroupLeader;
import learning_management_system.backend.entity.GroupMember;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;
import learning_management_system.backend.enums.GroupMemberRole;
import learning_management_system.backend.enums.LeadershipRole;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.GroupLeaderRepository;
import learning_management_system.backend.repository.GroupMemberRepository;
import learning_management_system.backend.repository.GroupSetRepository;
import learning_management_system.backend.repository.StudentGroupRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.GroupLeaderService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupLeaderServiceImplementation implements GroupLeaderService {
    private final GroupLeaderRepository leaders;
    private final GroupMemberRepository members;
    private final StudentGroupRepository groups;
    private final GroupSetRepository groupSets;
    private final CourseRepository courses;
    private final UserService userService;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public GroupLeaderServiceImplementation(GroupLeaderRepository leaders, GroupMemberRepository members, StudentGroupRepository groups,
                                            GroupSetRepository groupSets, CourseRepository courses, UserService userService,
                                            CourseAccess access, EntityRefresher refresher) {
        this.leaders = leaders;
        this.members = members;
        this.groups = groups;
        this.groupSets = groupSets;
        this.courses = courses;
        this.userService = userService;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupLeaderDto> list(UUID groupId) {
        GroupSet gs = setOf(groupId);
        access.requireCourseViewer(courses.findById(gs.getCourseId()).orElseThrow(() -> ApiException.notFound("Course")));
        return toDtos(leaders.findByGroupIdOrderByAppointedAtAsc(groupId));
    }

    @Transactional
    @Override
    public GroupLeaderDto appoint(UUID groupId, AppointGroupLeaderRequest r) {
        GroupSet gs = setOf(groupId);
        access.requireCourseEditor(gs.getCourseId());
        GroupMember m = members.findByGroupIdAndUserId(groupId, r.userId()).filter(x -> Boolean.TRUE.equals(x.getIsActive()))
                .orElseThrow(() -> ApiException.badRequest("NOT_A_MEMBER", "Only an active member of the group can lead it"));
        if (r.termEndAt() != null && !r.termEndAt().isAfter(OffsetDateTime.now())) {
            throw ApiException.badRequest("INVALID_TERM", "termEndAt must be in the future");
        }
        LeadershipRole role = r.leadershipRole() == null ? LeadershipRole.PROJECT_LEAD : r.leadershipRole();
        boolean duplicate = leaders.current(groupId).stream().anyMatch(l -> l.getUserId().equals(r.userId()) && l.getLeadershipRole() == role);
        if (duplicate) throw ApiException.conflict("ALREADY_LEADER", "The member already holds this role");
        GroupLeader l = create(groupId, r.userId(), role, Access.ctx().userId(), r.termEndAt());
        markMember(m, true);
        return toDtos(List.of(l)).get(0);
    }

    @Transactional
    @Override
    public GroupLeaderDto end(UUID groupId, UUID leaderId) {
        GroupSet gs = setOf(groupId);
        access.requireCourseEditor(gs.getCourseId());
        GroupLeader l = leaders.findByLeaderIdAndGroupId(leaderId, groupId).orElseThrow(() -> ApiException.notFound("Leader"));
        if (!isCurrent(l)) throw ApiException.conflict("ALREADY_ENDED", "This leadership has already ended");
        l.setTermEndAt(OffsetDateTime.now());
        leaders.saveAndFlush(l);
        refreshMemberRole(groupId, l.getUserId());
        return toDtos(List.of(l)).get(0);
    }

    @Override
    public void memberJoined(UUID groupId, UUID userId, boolean autoAssign) {
        if (!autoAssign || !leaders.current(groupId).isEmpty()) return;
        create(groupId, userId, LeadershipRole.PROJECT_LEAD, null, null);
        members.findByGroupIdAndUserId(groupId, userId).ifPresent(m -> markMember(m, true));
    }

    @Override
    public void memberLeft(UUID groupId, UUID userId, boolean autoAssign) {
        boolean wasLead = false;
        for (GroupLeader l : leaders.current(groupId)) {
            if (!l.getUserId().equals(userId)) continue;
            l.setTermEndAt(OffsetDateTime.now());
            leaders.save(l);
            wasLead |= l.getLeadershipRole() == LeadershipRole.PROJECT_LEAD;
        }
        leaders.flush();
        if (autoAssign && wasLead && leaders.current(groupId).stream().noneMatch(l -> l.getLeadershipRole() == LeadershipRole.PROJECT_LEAD)) {
            members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(groupId).stream().filter(m -> !m.getUserId().equals(userId)).findFirst()
                    .ifPresent(next -> {
                        create(groupId, next.getUserId(), LeadershipRole.PROJECT_LEAD, null, null);
                        markMember(next, true);
                    });
        }
    }

    // ---------------------------------------------------------------- helpers

    private GroupLeader create(UUID groupId, UUID userId, LeadershipRole role, UUID appointedBy, OffsetDateTime termEnd) {
        GroupLeader l = new GroupLeader();
        l.setGroupId(groupId);
        l.setUserId(userId);
        l.setLeadershipRole(role);
        l.setAppointedBy(appointedBy);
        l.setAppointedAt(OffsetDateTime.now());
        l.setTermEndAt(termEnd);
        return refresher.refresh(leaders.saveAndFlush(l));
    }

    /** The member's role follows their leaderships: LEADER while any is current, MEMBER otherwise. */
    private void refreshMemberRole(UUID groupId, UUID userId) {
        boolean leads = leaders.current(groupId).stream().anyMatch(l -> l.getUserId().equals(userId));
        members.findByGroupIdAndUserId(groupId, userId).filter(m -> Boolean.TRUE.equals(m.getIsActive())).ifPresent(m -> markMember(m, leads));
    }

    private void markMember(GroupMember m, boolean leader) {
        if (leader && m.getRole() != GroupMemberRole.LEADER) { m.setRole(GroupMemberRole.LEADER); members.saveAndFlush(m); }
        else if (!leader && m.getRole() == GroupMemberRole.LEADER) { m.setRole(GroupMemberRole.MEMBER); members.saveAndFlush(m); }
    }

    private static boolean isCurrent(GroupLeader l) {
        return l.getTermEndAt() == null || l.getTermEndAt().isAfter(OffsetDateTime.now());
    }

    private List<GroupLeaderDto> toDtos(List<GroupLeader> list) {
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(GroupLeader::getUserId).toList());
        return list.stream().map(l -> {
            UserSummaryDto u = names.get(l.getUserId());
            return new GroupLeaderDto(l.getLeaderId(), l.getGroupId(), l.getUserId(), u == null ? null : u.username(),
                    u == null ? null : u.displayName(), l.getLeadershipRole(), l.getAppointedAt(), l.getAppointedBy(), l.getTermEndAt(), isCurrent(l));
        }).toList();
    }

    private GroupSet setOf(UUID groupId) {
        StudentGroup g = groups.findById(groupId).orElseThrow(() -> ApiException.notFound("Group"));
        return groupSets.findById(g.getGroupSetId()).orElseThrow(() -> ApiException.notFound("Group set"));
    }
}
