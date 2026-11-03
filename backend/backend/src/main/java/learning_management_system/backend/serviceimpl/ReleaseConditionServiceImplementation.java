package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CreateReleaseConditionRequest;
import learning_management_system.backend.dto.ReleaseConditionDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.ConditionType;
import learning_management_system.backend.enums.ReleaseConditionSourceType;
import learning_management_system.backend.enums.TargetType;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.ReleaseConditionService;
import learning_management_system.backend.utility.EntityRefresher;
import learning_management_system.backend.utility.StudentWorkLookup;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ReleaseConditionServiceImplementation implements ReleaseConditionService {
    private static final Set<TargetType> TARGETS = EnumSet.of(TargetType.CONTENT, TargetType.MODULE, TargetType.ASSIGNMENT);

    private final ReleaseConditionRepository conditions;
    private final CourseRepository courses;
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final CourseModuleRepository modules;
    private final CourseSectionRepository sections;
    private final StudentGroupRepository groups;
    private final GroupSetRepository groupSets;
    private final GroupMemberRepository groupMembers;
    private final DifferentiationTagRepository tags;
    private final DifferentiationTagMemberRepository tagMembers;
    private final EnrollmentRepository enrollments;
    private final StudentWorkLookup work;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public ReleaseConditionServiceImplementation(ReleaseConditionRepository conditions, CourseRepository courses, ContentRepository contents,
                                                 AssignmentRepository assignments, CourseModuleRepository modules,
                                                 CourseSectionRepository sections, StudentGroupRepository groups, GroupSetRepository groupSets,
                                                 GroupMemberRepository groupMembers, DifferentiationTagRepository tags,
                                                 DifferentiationTagMemberRepository tagMembers, EnrollmentRepository enrollments,
                                                 StudentWorkLookup work, CourseAccess access, EntityRefresher refresher) {
        this.conditions = conditions;
        this.courses = courses;
        this.contents = contents;
        this.assignments = assignments;
        this.modules = modules;
        this.sections = sections;
        this.groups = groups;
        this.groupSets = groupSets;
        this.groupMembers = groupMembers;
        this.tags = tags;
        this.tagMembers = tagMembers;
        this.enrollments = enrollments;
        this.work = work;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReleaseConditionDto> list(UUID courseId, TargetType targetType, UUID targetId) {
        requireStaff(courseId);
        List<ReleaseCondition> all = targetType != null && targetId != null
                ? conditions.findByTargetTypeAndTargetIdOrderByConditionGroupAscDateCreatedAsc(targetType, targetId)
                : conditions.findByCourseIdOrderByTargetTypeAscConditionGroupAscDateCreatedAsc(courseId);
        return all.stream().filter(c -> c.getCourseId().equals(courseId)).map(ReleaseConditionServiceImplementation::toDto).toList();
    }

    @Transactional
    @Override
    public ReleaseConditionDto create(UUID courseId, CreateReleaseConditionRequest r) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        access.requireCourseEditor(courseId);
        if (!TARGETS.contains(r.targetType())) throw ApiException.badRequest("TARGET_NOT_SUPPORTED", r.targetType() + " targets are not supported yet");
        if (title(courseId, r.targetType().name(), r.targetId()) == null) throw ApiException.badRequest("TARGET_NOT_IN_COURSE", "The target is not part of this course");
        ReleaseCondition c = new ReleaseCondition();
        c.setCourseId(courseId);
        c.setTargetType(r.targetType());
        c.setTargetId(r.targetId());
        c.setConditionType(r.conditionType());
        c.setConditionGroup(r.conditionGroup() == null ? 1 : r.conditionGroup());
        switch (r.conditionType()) {
            case DATE -> {
                if (r.dateFrom() == null && r.dateUntil() == null) throw ApiException.badRequest("DATES_REQUIRED", "DATE conditions need dateFrom and/or dateUntil");
                if (r.dateFrom() != null && r.dateUntil() != null && !r.dateUntil().isAfter(r.dateFrom())) {
                    throw ApiException.badRequest("INVALID_DATES", "dateUntil must be after dateFrom");
                }
                c.setDateFrom(r.dateFrom());
                c.setDateUntil(r.dateUntil());
            }
            case MEMBER -> {
                int n = (r.memberUserId() != null ? 1 : 0) + (r.memberGroupId() != null ? 1 : 0) + (r.memberSectionId() != null ? 1 : 0) + (r.memberTagId() != null ? 1 : 0);
                if (n != 1) throw ApiException.badRequest("ONE_MEMBER", "MEMBER conditions need exactly one of memberUserId, memberGroupId, memberSectionId, memberTagId");
                if (r.memberSectionId() != null && sections.findById(r.memberSectionId()).filter(s -> s.getCourseId().equals(courseId)).isEmpty()) {
                    throw ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course");
                }
                if (r.memberGroupId() != null && groups.findById(r.memberGroupId())
                        .flatMap(g -> groupSets.findById(g.getGroupSetId())).filter(gs -> gs.getCourseId().equals(courseId)).isEmpty()) {
                    throw ApiException.badRequest("GROUP_NOT_IN_COURSE", "The group does not belong to this course");
                }
                if (r.memberTagId() != null && tags.findById(r.memberTagId()).filter(t -> t.getCourseId().equals(courseId)).isEmpty()) {
                    throw ApiException.badRequest("TAG_NOT_IN_COURSE", "The tag does not belong to this course");
                }
                if (r.memberUserId() != null && enrollments.activeInCourse(courseId, r.memberUserId()).isEmpty()) {
                    throw ApiException.badRequest("NOT_IN_COURSE", "The user is not in this course");
                }
                c.setMemberUserId(r.memberUserId());
                c.setMemberGroupId(r.memberGroupId());
                c.setMemberSectionId(r.memberSectionId());
                c.setMemberTagId(r.memberTagId());
            }
            default -> {
                ReleaseConditionSourceType st = r.sourceType();
                if (st == null || r.sourceId() == null) throw ApiException.badRequest("SOURCE_REQUIRED", r.conditionType() + " conditions need sourceType and sourceId");
                boolean ok = switch (r.conditionType()) {
                    case PERFORMANCE, SUBMITTED -> st == ReleaseConditionSourceType.ASSIGNMENT;
                    case VIEWED -> st == ReleaseConditionSourceType.CONTENT;
                    case COMPLETED -> st == ReleaseConditionSourceType.CONTENT || st == ReleaseConditionSourceType.MODULE;
                    default -> false;
                };
                if (!ok) throw ApiException.badRequest("SOURCE_NOT_SUPPORTED", r.conditionType() + " conditions do not take " + st + " sources (yet)");
                if (r.sourceId().equals(r.targetId())) throw ApiException.badRequest("SELF_CONDITION", "An item cannot depend on itself");
                if (title(courseId, st.name(), r.sourceId()) == null) throw ApiException.badRequest("SOURCE_NOT_IN_COURSE", "The source is not part of this course");
                if (r.conditionType() == ConditionType.PERFORMANCE) {
                    if (r.minScorePercent() == null && r.maxScorePercent() == null) {
                        throw ApiException.badRequest("SCORE_REQUIRED", "PERFORMANCE conditions need minScorePercent and/or maxScorePercent");
                    }
                    if (r.minScorePercent() != null && r.maxScorePercent() != null && r.maxScorePercent().compareTo(r.minScorePercent()) < 0) {
                        throw ApiException.badRequest("INVALID_SCORES", "maxScorePercent must be at least minScorePercent");
                    }
                    c.setMinScorePercent(r.minScorePercent());
                    c.setMaxScorePercent(r.maxScorePercent());
                }
                c.setSourceType(st);
                c.setSourceId(r.sourceId());
            }
        }
        return toDto(refresher.refresh(conditions.saveAndFlush(c)));
    }

    @Transactional
    @Override
    public void delete(UUID courseId, UUID conditionId) {
        access.requireCourseEditor(courseId);
        conditions.delete(conditions.findByConditionIdAndCourseId(conditionId, courseId).orElseThrow(() -> ApiException.notFound("Condition")));
    }

    @Override
    public String lockReason(UUID courseId, TargetType targetType, UUID targetId, UUID userId) {
        List<ReleaseCondition> all = conditions.findByTargetTypeAndTargetIdOrderByConditionGroupAscDateCreatedAsc(targetType, targetId);
        if (all.isEmpty()) return null;
        Map<Integer, List<ReleaseCondition>> byGroup = new TreeMap<>();
        all.forEach(c -> byGroup.computeIfAbsent(c.getConditionGroup(), k -> new ArrayList<>()).add(c));
        String first = null;
        for (List<ReleaseCondition> group : byGroup.values()) {
            String unmet = null;
            for (ReleaseCondition c : group) {
                unmet = unmet(c, courseId, userId);
                if (unmet != null) break;
            }
            if (unmet == null) return null;                    // one group is fully met
            if (first == null) first = unmet;
        }
        return byGroup.size() > 1 ? "Not available yet: " + first + " (or meet another set of conditions)" : first;
    }

    // ---------------------------------------------------------------- evaluation

    private String unmet(ReleaseCondition c, UUID courseId, UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        switch (c.getConditionType()) {
            case DATE -> {
                if (c.getDateFrom() != null && now.isBefore(c.getDateFrom())) return "Available from " + c.getDateFrom();
                if (c.getDateUntil() != null && !now.isBefore(c.getDateUntil())) return "Closed on " + c.getDateUntil();
                return null;
            }
            case MEMBER -> {
                boolean in;
                if (c.getMemberUserId() != null) in = c.getMemberUserId().equals(userId);
                else if (c.getMemberSectionId() != null) in = enrollments.activeInCourse(courseId, userId).stream().anyMatch(e -> e.getSectionId().equals(c.getMemberSectionId()));
                else if (c.getMemberGroupId() != null) in = groupMembers.findByGroupIdAndUserId(c.getMemberGroupId(), userId).filter(m -> Boolean.TRUE.equals(m.getIsActive())).isPresent();
                else in = tagMembers.existsByTagIdAndUserId(c.getMemberTagId(), userId);
                return in ? null : "Not available to you";
            }
            case SUBMITTED -> {
                return work.handedIn(c.getSourceId(), userId) ? null : "Submit \"" + name(courseId, c) + "\" first";
            }
            case VIEWED -> {
                return work.viewed(c.getSourceId(), userId) ? null : "View \"" + name(courseId, c) + "\" first";
            }
            case COMPLETED -> {
                boolean done = c.getSourceType() == ReleaseConditionSourceType.MODULE ? work.moduleCompleted(c.getSourceId(), userId)
                        : work.markedDone(c.getSourceId(), userId);
                return done ? null : "Complete \"" + name(courseId, c) + "\" first";
            }
            case PERFORMANCE -> {
                BigDecimal p = work.postedPercent(c.getSourceId(), userId);
                String range = c.getMinScorePercent() != null && c.getMaxScorePercent() != null
                        ? "between " + c.getMinScorePercent().stripTrailingZeros().toPlainString() + "% and " + c.getMaxScorePercent().stripTrailingZeros().toPlainString() + "%"
                        : c.getMinScorePercent() != null ? "at least " + c.getMinScorePercent().stripTrailingZeros().toPlainString() + "%"
                        : "at most " + c.getMaxScorePercent().stripTrailingZeros().toPlainString() + "%";
                String need = "Score " + range + " on \"" + name(courseId, c) + "\"";
                if (p == null) return need + " (not graded yet)";
                if (c.getMinScorePercent() != null && p.compareTo(c.getMinScorePercent()) < 0) return need;
                if (c.getMaxScorePercent() != null && p.compareTo(c.getMaxScorePercent()) > 0) return need;
                return null;
            }
            default -> {
                return "Not available";
            }
        }
    }

    private String name(UUID courseId, ReleaseCondition c) {
        String t = title(courseId, c.getSourceType().name(), c.getSourceId());
        return t == null ? "an item" : t;
    }

    /** The item's title when it belongs to the course, else null. */
    private String title(UUID courseId, String type, UUID id) {
        return switch (type) {
            case "CONTENT" -> contents.findById(id).filter(x -> x.getCourseId().equals(courseId)).map(Content::getContentTitle).orElse(null);
            case "ASSIGNMENT" -> assignments.findById(id).filter(x -> x.getCourseId().equals(courseId)).map(Assignment::getAssignmentTitle).orElse(null);
            case "MODULE" -> modules.findById(id).filter(x -> x.getCourseId().equals(courseId)).map(CourseModule::getModuleTitle).orElse(null);
            default -> null;
        };
    }

    private void requireStaff(UUID courseId) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        learning_management_system.backend.security.Access.requireTenant();
        if (!access.canSeeEverything() && !access.isCourseStaff(courseId)) throw ApiException.forbidden("Only course staff can see release conditions");
    }

    private static ReleaseConditionDto toDto(ReleaseCondition c) {
        return new ReleaseConditionDto(c.getConditionId(), c.getCourseId(), c.getTargetType(), c.getTargetId(), c.getConditionType(), c.getDateFrom(),
                c.getDateUntil(), c.getMemberUserId(), c.getMemberGroupId(), c.getMemberSectionId(), c.getMemberTagId(), c.getSourceType(),
                c.getSourceId(), c.getMinScorePercent(), c.getMaxScorePercent(), c.getConditionGroup(), c.getDateCreated());
    }
}
