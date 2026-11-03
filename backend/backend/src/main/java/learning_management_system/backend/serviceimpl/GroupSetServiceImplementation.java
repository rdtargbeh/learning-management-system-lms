package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.*;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.GroupMapper;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.GroupMemberService;
import learning_management_system.backend.service.GroupSetService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupSetServiceImplementation implements GroupSetService {
    private final GroupSetRepository groupSets;
    private final StudentGroupRepository groups;
    private final GroupMemberRepository members;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final EnrollmentRepository enrollments;
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final GroupMemberService memberService;
    private final CourseAccess access;
    private final GroupMapper groupMapper;
    private final EntityRefresher refresher;

    public GroupSetServiceImplementation(GroupSetRepository groupSets, StudentGroupRepository groups, GroupMemberRepository members,
                                         CourseRepository courses, CourseSectionRepository sections, EnrollmentRepository enrollments,
                                         AssignmentRepository assignments, AssignmentSubmissionRepository submissions,
                                         GroupMemberService memberService, CourseAccess access, GroupMapper groupMapper,
                                         EntityRefresher refresher) {
        this.groupSets = groupSets;
        this.groups = groups;
        this.members = members;
        this.courses = courses;
        this.sections = sections;
        this.enrollments = enrollments;
        this.assignments = assignments;
        this.submissions = submissions;
        this.memberService = memberService;
        this.access = access;
        this.groupMapper = groupMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupSetDto> list(UUID courseId) {
        access.requireCourseViewer(course(courseId));
        return groupSets.findByCourseIdOrderBySetNameAsc(courseId).stream().map(groupMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public GroupSetDto get(UUID groupSetId) {
        GroupSet gs = set(groupSetId);
        access.requireCourseViewer(course(gs.getCourseId()));
        return groupMapper.toDto(gs);
    }

    @Transactional
    @Override
    public GroupSetDto create(UUID courseId, CreateGroupSetRequest r) {
        course(courseId);
        access.requireCourseEditor(courseId);
        if (groupSets.existsByCourseIdAndSetNameIgnoreCase(courseId, r.setName().trim())) {
            throw ApiException.conflict("SET_NAME_TAKEN", "The course already has a group set with this name");
        }
        GroupSet gs = groupMapper.toEntity(r);
        gs.setCourseId(courseId);
        gs.setSetName(r.setName().trim());
        gs.setCreatedBy(Access.ctx().userId());
        return groupMapper.toDto(refresher.refresh(groupSets.saveAndFlush(gs)));
    }

    @Transactional
    @Override
    public GroupSetDto update(UUID groupSetId, UpdateGroupSetRequest r) {
        GroupSet gs = set(groupSetId);
        access.requireCourseEditor(gs.getCourseId());
        if (r.setName() != null && !r.setName().trim().equalsIgnoreCase(gs.getSetName())
                && groupSets.existsByCourseIdAndSetNameIgnoreCase(gs.getCourseId(), r.setName().trim())) {
            throw ApiException.conflict("SET_NAME_TAKEN", "The course already has a group set with this name");
        }
        if (Boolean.TRUE.equals(r.restrictToSection()) && !Boolean.TRUE.equals(gs.getRestrictToSection())) {
            // every current member must already be in their group's section
            for (StudentGroup g : groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId)) {
                for (GroupMember m : members.findByGroupIdAndIsActiveTrueOrderByJoinedAtAsc(g.getGroupId())) {
                    if (enrollments.findBySectionIdAndUserId(g.getSectionId(), m.getUserId()).isEmpty()) {
                        throw ApiException.conflict("MEMBERS_IN_OTHER_SECTIONS", "Some members are not in their group's section");
                    }
                }
            }
        }
        groupMapper.updateEntity(r, gs);
        if (r.setName() != null) gs.setSetName(r.setName().trim());
        return groupMapper.toDto(groupSets.saveAndFlush(gs));
    }

    @Transactional
    @Override
    public void delete(UUID groupSetId) {
        GroupSet gs = set(groupSetId);
        access.requireCourseEditor(gs.getCourseId());
        if (assignments.existsByGroupSetId(groupSetId)) {
            throw ApiException.conflict("GROUP_SET_IN_USE", "A group assignment uses this set");
        }
        for (StudentGroup g : groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId)) {
            if (submissions.existsByGroupId(g.getGroupId())) {
                throw ApiException.conflict("GROUP_HAS_SUBMISSIONS", "A group of this set has handed in work");
            }
        }
        groupSets.delete(gs);
    }

    @Transactional
    @Override
    public AutoAssignGroupsResultDto autoAssign(UUID groupSetId, AutoAssignGroupsRequest r) {
        GroupSet gs = set(groupSetId);
        access.requireCourseEditor(gs.getCourseId());
        List<CourseSection> courseSections = sections.findByCourseIdOrderBySectionNumberAsc(gs.getCourseId());
        if (courseSections.isEmpty()) throw ApiException.conflict("NO_SECTIONS", "The course has no sections");
        boolean bySection = Boolean.TRUE.equals(gs.getRestrictToSection());
        int wanted = r == null || r.groupCount() == null ? 0 : r.groupCount();

        // students not yet in a group of the set, by section (one bucket when the set is not restricted)
        Set<UUID> placed = new HashSet<>(members.activeUserIdsInSet(groupSetId));
        Map<UUID, List<UUID>> waiting = new LinkedHashMap<>();
        for (CourseSection s : courseSections) {
            UUID bucket = bySection ? s.getSectionId() : courseSections.get(0).getSectionId();
            for (Enrollment e : enrollments.roster(s.getSectionId(), learning_management_system.backend.enums.EnrollmentStatus.ACTIVE, null)) {
                if (!EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType()) || !placed.add(e.getUserId())) continue;
                waiting.computeIfAbsent(bucket, k -> new ArrayList<>()).add(e.getUserId());
            }
        }

        int created = 0, assigned = 0, left = 0;
        int counter = groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId).size();
        for (CourseSection s : bySection ? courseSections : List.of(courseSections.get(0))) {
            List<StudentGroup> pool = groups.findByGroupSetIdAndIsActiveTrue(groupSetId).stream()
                    .filter(g -> !bySection || g.getSectionId().equals(s.getSectionId()))
                    .collect(Collectors.toCollection(ArrayList::new));
            while (pool.size() < wanted) {
                StudentGroup g = new StudentGroup();
                g.setGroupSetId(groupSetId);
                g.setSectionId(s.getSectionId());
                g.setGroupName(gs.getSetName() + " " + (++counter));
                g.setMaxMembers(gs.getMaxMembersPerGroup());
                g.setDateCreatedGroup(LocalDate.now());
                pool.add(refresher.refresh(groups.saveAndFlush(g)));
                created++;
            }
            List<UUID> students = new ArrayList<>(waiting.getOrDefault(s.getSectionId(), List.of()));
            Collections.shuffle(students);
            for (UUID student : students) {
                StudentGroup target = pool.stream()
                        .filter(g -> room(g, gs) > 0 && memberService.isEligible(gs, g.getSectionId(), student))
                        .min(Comparator.comparingLong(g -> members.countByGroupIdAndIsActiveTrue(g.getGroupId())))
                        .orElse(null);
                if (target == null) { left++; continue; }
                memberService.join(groups.lockById(target.getGroupId()).orElseThrow(), gs, student);
                assigned++;
            }
        }
        List<StudentGroupDto> all = groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId).stream().map(groupMapper::toDto).toList();
        return new AutoAssignGroupsResultDto(created, assigned, left, all);
    }

    /** Free places in a group (Long.MAX_VALUE without a limit). */
    private long room(StudentGroup g, GroupSet gs) {
        Integer limit = g.getMaxMembers() != null ? g.getMaxMembers() : gs.getMaxMembersPerGroup();
        return limit == null ? Long.MAX_VALUE : limit - members.countByGroupIdAndIsActiveTrue(g.getGroupId());
    }

    private GroupSet set(UUID id) {
        return groupSets.findById(id).orElseThrow(() -> ApiException.notFound("Group set"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
