package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AddGroupMemberRequest;
import learning_management_system.backend.dto.CreateGroupSetRequest;
import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.GroupMemberDto;
import learning_management_system.backend.dto.GroupSetDto;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.dto.UserSummaryDto;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.entity.GroupMember;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.GroupMemberRole;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.GroupMapper;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.GroupMemberRepository;
import learning_management_system.backend.repository.GroupSetRepository;
import learning_management_system.backend.repository.StudentGroupRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.GroupService;
import learning_management_system.backend.service.UserService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupServiceImplementation implements GroupService {
    private final GroupSetRepository groupSets;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository members;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final UserService userService;
    private final CourseAccess access;
    private final GroupMapper groupMapper;
    private final EntityRefresher refresher;

    public GroupServiceImplementation(GroupSetRepository groupSets, StudentGroupRepository groups,
                                      GroupMemberRepository members, CourseRepository courses,
                                      CourseSectionRepository sections, EnrollmentRepository enrollments,
                                      UserService userService, CourseAccess access, GroupMapper groupMapper,
                                      EntityRefresher refresher) {
        this.groupSets = groupSets;
        this.groups = groups;
        this.members = members;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.userService = userService;
        this.access = access;
        this.groupMapper = groupMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupSetDto> listSets(UUID courseId) {
        access.requireCourseViewer(course(courseId));
        return groupSets.findByCourseIdOrderBySetNameAsc(courseId).stream().map(groupMapper::toDto).toList();
    }

    @Transactional
    @Override
    public GroupSetDto createSet(UUID courseId, CreateGroupSetRequest r) {
        course(courseId);
        access.requireCourseEditor(courseId);
        GroupSet gs = groupMapper.toEntity(r);
        gs.setCourseId(courseId);
        gs.setSetName(r.setName().trim());
        gs.setCreatedBy(Access.ctx().userId());
        return groupMapper.toDto(refresher.refresh(groupSets.saveAndFlush(gs)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<StudentGroupDto> listGroups(UUID groupSetId) {
        GroupSet gs = set(groupSetId);
        access.requireCourseViewer(course(gs.getCourseId()));
        return groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId).stream().map(groupMapper::toDto).toList();
    }

    @Transactional
    @Override
    public StudentGroupDto createGroup(UUID groupSetId, CreateStudentGroupRequest r) {
        GroupSet gs = set(groupSetId);
        access.requireCourseEditor(gs.getCourseId());
        CourseSection s = sections.findById(r.sectionId()).filter(x -> x.getCourseId().equals(gs.getCourseId()))
                .orElseThrow(() -> ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course"));
        if (r.dateStart() != null && r.dateEnd() != null && r.dateEnd().isBefore(r.dateStart())) {
            throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
        }
        StudentGroup g = groupMapper.toEntity(r);
        g.setGroupSetId(groupSetId);
        g.setSectionId(s.getSectionId());
        g.setGroupName(r.groupName().trim());
        g.setMaxMembers(r.maxMembers() != null ? r.maxMembers() : gs.getMaxMembersPerGroup());
        g.setDateCreatedGroup(LocalDate.now());
        return groupMapper.toDto(refresher.refresh(groups.saveAndFlush(g)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupMemberDto> members(UUID groupId) {
        StudentGroup g = groups.findById(groupId).orElseThrow(() -> ApiException.notFound("Group"));
        access.requireCourseViewer(course(set(g.getGroupSetId()).getCourseId()));
        List<GroupMember> list = members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(groupId);
        Map<UUID, UserSummaryDto> names = userService.summariesById(list.stream().map(GroupMember::getUserId).toList());
        return list.stream().map(m -> toDto(m, names.get(m.getUserId()))).toList();
    }

    @Transactional
    @Override
    public GroupMemberDto addMember(UUID groupId, AddGroupMemberRequest r) {
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
        if (Boolean.FALSE.equals(g.getIsActive())) throw ApiException.conflict("GROUP_INACTIVE", "The group is not active");
        requireEnrolledStudent(gs, g, userId);
        GroupMember existingInSet = members.activeInSet(gs.getGroupSetId(), userId).orElse(null);
        if (existingInSet != null) {
            throw ApiException.conflict("ALREADY_IN_GROUP", existingInSet.getGroupId().equals(groupId)
                    ? "Already a member of this group" : "Already in another group of this set");
        }
        long count = members.countByGroupIdAndIsActiveTrue(groupId);
        Integer limit = g.getMaxMembers() != null ? g.getMaxMembers() : gs.getMaxMembersPerGroup();
        if (limit != null && count >= limit) throw ApiException.conflict("GROUP_FULL", "The group is full");

        GroupMember m = members.findByGroupIdAndUserId(groupId, userId).orElseGet(GroupMember::new);
        m.setGroupId(groupId);
        m.setUserId(userId);
        GroupMemberRole role = r != null && r.role() != null && !self ? r.role() : GroupMemberRole.MEMBER;
        if (count == 0 && Boolean.TRUE.equals(gs.getAutoAssignLeader())) role = GroupMemberRole.LEADER;
        m.setRole(role);
        m.setIsActive(true);
        m.setJoinedAt(OffsetDateTime.now());
        m.setLeftAt(null);
        refresher.refresh(members.saveAndFlush(m));
        recount(g);
        return toDto(m, userService.summariesById(List.of(userId)).get(userId));
    }

    @Transactional
    @Override
    public void removeMember(UUID groupId, UUID userId) {
        Access.requireTenant();
        StudentGroup g = lockGroup(groupId);
        GroupSet gs = set(g.getGroupSetId());
        if (!(Access.isSelf(userId) && Boolean.TRUE.equals(gs.getAllowSelfSignup()))) access.requireCourseEditor(gs.getCourseId());
        GroupMember m = members.findByGroupIdAndUserId(groupId, userId).filter(x -> Boolean.TRUE.equals(x.getIsActive()))
                .orElseThrow(() -> ApiException.notFound("Group member"));
        m.setIsActive(false);
        m.setLeftAt(OffsetDateTime.now());
        members.saveAndFlush(m);
        recount(g);
    }

    // ---------------------------------------------------------------- helpers

    /** Members must be actively enrolled students: in the group's section when the set is restricted, else in the course. */
    private void requireEnrolledStudent(GroupSet gs, StudentGroup g, UUID userId) {
        boolean ok;
        if (Boolean.TRUE.equals(gs.getRestrictToSection())) {
            ok = enrollments.findBySectionIdAndUserId(g.getSectionId(), userId).filter(this::activeStudent).isPresent();
        } else {
            ok = sections.findByCourseIdOrderBySectionNumberAsc(gs.getCourseId()).stream()
                    .anyMatch(s -> enrollments.findBySectionIdAndUserId(s.getSectionId(), userId).filter(this::activeStudent).isPresent());
        }
        if (!ok) throw ApiException.badRequest("NOT_ENROLLED", "Only students enrolled in the course can be in its groups");
    }

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
