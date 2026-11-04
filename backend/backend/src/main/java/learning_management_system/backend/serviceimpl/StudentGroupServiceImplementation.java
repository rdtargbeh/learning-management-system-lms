package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateStudentGroupRequest;
import learning_management_system.backend.dto.StudentGroupDto;
import learning_management_system.backend.dto.UpdateStudentGroupRequest;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.GroupSet;
import learning_management_system.backend.entity.StudentGroup;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.GroupMapper;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.StudentGroupService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StudentGroupServiceImplementation implements StudentGroupService {
    private final StudentGroupRepository groups;
    private final GroupSetRepository groupSets;
    private final GroupMemberRepository members;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final AssignmentSubmissionRepository submissions;
    private final CourseAccess access;
    private final GroupMapper groupMapper;
    private final EntityRefresher refresher;

    public StudentGroupServiceImplementation(StudentGroupRepository groups, GroupSetRepository groupSets, GroupMemberRepository members,
                                             CourseRepository courses, CourseSectionRepository sections,
                                             AssignmentSubmissionRepository submissions, CourseAccess access, GroupMapper groupMapper,
                                             EntityRefresher refresher) {
        this.groups = groups;
        this.groupSets = groupSets;
        this.members = members;
        this.courses = courses;
        this.sections = sections;
        this.submissions = submissions;
        this.access = access;
        this.groupMapper = groupMapper;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<StudentGroupDto> list(UUID groupSetId) {
        GroupSet gs = set(groupSetId);
        access.requireCourseViewer(course(gs.getCourseId()));
        return groups.findByGroupSetIdOrderByGroupNameAsc(groupSetId).stream().map(groupMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public StudentGroupDto get(UUID groupId) {
        StudentGroup g = group(groupId);
        access.requireCourseViewer(course(set(g.getGroupSetId()).getCourseId()));
        return groupMapper.toDto(g);
    }

    @Transactional
    @Override
    public StudentGroupDto create(UUID groupSetId, CreateStudentGroupRequest r) {
        GroupSet gs = set(groupSetId);
        access.requireCourseEditor(gs.getCourseId());
        CourseSection s = sections.findById(r.sectionId()).filter(x -> x.getCourseId().equals(gs.getCourseId()))
                .orElseThrow(() -> ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course"));
        validateDates(r.dateStart(), r.dateEnd());
        StudentGroup g = groupMapper.toEntity(r);
        g.setGroupSetId(groupSetId);
        g.setSectionId(s.getSectionId());
        g.setGroupName(r.groupName().trim());
        g.setMaxMembers(r.maxMembers() != null ? r.maxMembers() : gs.getMaxMembersPerGroup());
        g.setDateCreatedGroup(LocalDate.now());
        return groupMapper.toDto(refresher.refresh(groups.saveAndFlush(g)));
    }

    @Transactional
    @Override
    public StudentGroupDto update(UUID groupId, UpdateStudentGroupRequest r) {
        StudentGroup g = groups.lockById(groupId).orElseThrow(() -> ApiException.notFound("Group"));
        access.requireCourseEditor(set(g.getGroupSetId()).getCourseId());
        if (r.maxMembers() != null && r.maxMembers() < members.countByGroupIdAndIsActiveTrue(groupId)) {
            throw ApiException.conflict("TOO_MANY_MEMBERS", "The group already has more members than that");
        }
        groupMapper.updateEntity(r, g);
        if (r.groupName() != null) g.setGroupName(r.groupName().trim());
        validateDates(g.getDateStart(), g.getDateEnd());
        return groupMapper.toDto(groups.saveAndFlush(g));
    }

    @Transactional
    @Override
    public void delete(UUID groupId) {
        StudentGroup g = group(groupId);
        access.requireCourseEditor(set(g.getGroupSetId()).getCourseId());
        if (submissions.existsByGroupId(groupId)) {
            throw ApiException.conflict("GROUP_HAS_SUBMISSIONS", "The group has handed in work; deactivate it instead");
        }
        groups.delete(g);                         // members and leaders go with it
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
    }

    private StudentGroup group(UUID id) {
        return groups.findById(id).orElseThrow(() -> ApiException.notFound("Group"));
    }

    private GroupSet set(UUID id) {
        return groupSets.findById(id).orElseThrow(() -> ApiException.notFound("Group set"));
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
