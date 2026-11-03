package learning_management_system.backend.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import learning_management_system.backend.dto.CoursePaceDto;
import learning_management_system.backend.dto.CoursePaceItemDto;
import learning_management_system.backend.dto.CoursePaceRequest;
import learning_management_system.backend.dto.PaceScheduleDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.enums.PaceStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.*;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CoursePaceService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CoursePaceServiceImplementation implements CoursePaceService {
    private final CoursePaceRepository paces;
    private final CoursePaceItemRepository paceItems;
    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final CourseModuleRepository modules;
    private final ContentRepository contents;
    private final AssignmentRepository assignments;
    private final EnrollmentRepository enrollments;
    private final CourseAccess access;
    private final EntityRefresher refresher;

    public CoursePaceServiceImplementation(CoursePaceRepository paces, CoursePaceItemRepository paceItems, CourseRepository courses,
                                           CourseSectionRepository sections, CourseModuleRepository modules, ContentRepository contents,
                                           AssignmentRepository assignments, EnrollmentRepository enrollments, CourseAccess access,
                                           EntityRefresher refresher) {
        this.paces = paces;
        this.paceItems = paceItems;
        this.courses = courses;
        this.sections = sections;
        this.modules = modules;
        this.contents = contents;
        this.assignments = assignments;
        this.enrollments = enrollments;
        this.access = access;
        this.refresher = refresher;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CoursePaceDto> list(UUID courseId) {
        requireStaff(courseId);
        return paces.findByCourseIdOrderByDateCreatedAsc(courseId).stream().map(this::toDto).toList();
    }

    @Transactional
    @Override
    public CoursePaceDto create(UUID courseId, CoursePaceRequest r) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        access.requireCourseEditor(courseId);
        if (r.sectionId() != null && r.userId() != null) throw ApiException.badRequest("ONE_SCOPE", "A pace is for the course, one section or one student");
        if (r.sectionId() != null && sections.findById(r.sectionId()).filter(s -> s.getCourseId().equals(courseId)).isEmpty()) {
            throw ApiException.badRequest("SECTION_NOT_IN_COURSE", "The section does not belong to this course");
        }
        if (r.userId() != null && enrollments.activeInCourse(courseId, r.userId()).stream()
                .noneMatch(e -> EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType()))) {
            throw ApiException.badRequest("NOT_A_STUDENT", "The user is not a student of this course");
        }
        boolean taken = paces.findByCourseIdOrderByDateCreatedAsc(courseId).stream()
                .anyMatch(p -> p.getPaceStatus() != PaceStatus.ARCHIVED && Objects.equals(p.getSectionId(), r.sectionId()) && Objects.equals(p.getUserId(), r.userId()));
        if (taken) throw ApiException.conflict("PACE_EXISTS", "This course, section or student already has a pace; edit or archive it");
        validateBlackouts(r.blackoutDates());
        CoursePace p = new CoursePace();
        p.setCourseId(courseId);
        p.setSectionId(r.sectionId());
        p.setUserId(r.userId());
        p.setPaceStatus(PaceStatus.DRAFT);
        p.setExcludeWeekends(r.excludeWeekends() == null || r.excludeWeekends());
        p.setBlackoutDates(r.blackoutDates());
        p.setDateEnd(r.dateEnd());
        p = refresher.refresh(paces.saveAndFlush(p));
        sync(p);
        return toDto(p);
    }

    @Transactional(readOnly = true)
    @Override
    public CoursePaceDto get(UUID paceId) {
        CoursePace p = load(paceId);
        requireStaff(p.getCourseId());
        return toDto(p);
    }

    @Transactional
    @Override
    public CoursePaceDto update(UUID paceId, CoursePaceRequest r) {
        CoursePace p = editable(paceId);
        if (r.sectionId() != null || r.userId() != null) throw ApiException.badRequest("SCOPE_FIXED", "The scope of a pace cannot change");
        validateBlackouts(r.blackoutDates());
        if (r.excludeWeekends() != null) p.setExcludeWeekends(r.excludeWeekends());
        if (r.blackoutDates() != null) p.setBlackoutDates(r.blackoutDates());
        if (r.dateEnd() != null) p.setDateEnd(r.dateEnd());
        return toDto(paces.saveAndFlush(p));
    }

    @Transactional
    @Override
    public CoursePaceDto refresh(UUID paceId) {
        CoursePace p = editable(paceId);
        sync(p);
        return toDto(p);
    }

    @Transactional
    @Override
    public CoursePaceDto publish(UUID paceId) {
        CoursePace p = editable(paceId);
        if (paceItems.findByPaceIdOrderByDisplayOrderAsc(paceId).isEmpty()) throw ApiException.conflict("EMPTY_PACE", "The pace has no items");
        for (CoursePace other : paces.findByCourseIdAndPaceStatus(p.getCourseId(), PaceStatus.PUBLISHED)) {
            if (!other.getPaceId().equals(paceId) && Objects.equals(other.getSectionId(), p.getSectionId()) && Objects.equals(other.getUserId(), p.getUserId())) {
                other.setPaceStatus(PaceStatus.ARCHIVED);
                paces.save(other);
            }
        }
        p.setPaceStatus(PaceStatus.PUBLISHED);
        p.setDatePublished(OffsetDateTime.now());
        return toDto(paces.saveAndFlush(p));
    }

    @Transactional
    @Override
    public CoursePaceDto archive(UUID paceId) {
        CoursePace p = editable(paceId);
        p.setPaceStatus(PaceStatus.ARCHIVED);
        return toDto(paces.saveAndFlush(p));
    }

    @Transactional
    @Override
    public void delete(UUID paceId) {
        CoursePace p = load(paceId);
        access.requireCourseEditor(p.getCourseId());
        if (p.getPaceStatus() != PaceStatus.DRAFT) throw ApiException.conflict("NOT_A_DRAFT", "Only draft paces can be deleted; archive it instead");
        paces.delete(p);
    }

    @Transactional(readOnly = true)
    @Override
    public PaceScheduleDto schedule(UUID paceId, UUID userId) {
        CoursePace p = load(paceId);
        requireStaff(p.getCourseId());
        return compute(p, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public PaceScheduleDto mine(UUID courseId) {
        access.requireCourseViewer(courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course")));
        UUID me = Access.ctx().userId();
        CoursePace p = applicable(courseId, me);
        if (p == null) throw ApiException.notFound("Pace");
        return compute(p, me);
    }

    @Override
    public LocalDate pacedDue(UUID courseId, UUID assignmentId, UUID userId) {
        CoursePace p = applicable(courseId, userId);
        if (p == null) return null;
        return compute(p, userId).items().stream().filter(s -> "ASSIGNMENT".equals(s.itemType()) && assignmentId.equals(s.itemId()))
                .map(PaceScheduleDto.Step::dueDate).findFirst().orElse(null);
    }

    // ---------------------------------------------------------------- scheduling

    /** The published pace for the student: their own, else their section's, else the course's. */
    private CoursePace applicable(UUID courseId, UUID userId) {
        List<CoursePace> published = paces.findByCourseIdAndPaceStatus(courseId, PaceStatus.PUBLISHED);
        if (published.isEmpty()) return null;
        List<Enrollment> mine = enrollments.activeInCourse(courseId, userId).stream()
                .filter(e -> EnrollmentProvisioningServiceImplementation.SEAT_TYPES.contains(e.getEnrollmentType())).toList();
        if (mine.isEmpty()) return null;
        Set<UUID> mySections = new HashSet<>();
        mine.forEach(e -> mySections.add(e.getSectionId()));
        return published.stream().filter(p -> userId.equals(p.getUserId())).findFirst()
                .or(() -> published.stream().filter(p -> p.getSectionId() != null && mySections.contains(p.getSectionId())).findFirst())
                .or(() -> published.stream().filter(p -> p.getSectionId() == null && p.getUserId() == null).findFirst())
                .orElse(null);
    }

    private PaceScheduleDto compute(CoursePace p, UUID userId) {
        Course c = courses.findById(p.getCourseId()).orElseThrow();
        ZoneId zone = zone(c);
        LocalDate start = LocalDate.now(zone);
        if (userId != null) {
            LocalDate enrolled = enrollments.inCourseWithStatuses(p.getCourseId(), userId, EnumSet.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED))
                    .stream().map(e -> e.getDateStarted() != null ? e.getDateStarted() : e.getDateEnrolled()).filter(Objects::nonNull)
                    .min(Comparator.naturalOrder()).orElse(null);
            if (enrolled != null) start = enrolled;
        }
        if (c.getDateStart() != null) {
            LocalDate courseStart = c.getDateStart().atZoneSameInstant(zone).toLocalDate();
            if (courseStart.isAfter(start)) start = courseStart;
        }
        Set<LocalDate> blackout = blackouts(p.getBlackoutDates());
        boolean noWeekends = !Boolean.FALSE.equals(p.getExcludeWeekends());
        start = firstWorkday(start, noWeekends, blackout);
        List<CoursePaceItem> items = paceItems.findByPaceIdOrderByDisplayOrderAsc(p.getPaceId());
        int total = items.stream().mapToInt(i -> i.getDurationDays() == null ? 0 : i.getDurationDays()).sum();
        double factor = 1.0;
        boolean compressed = false;
        if (p.getDateEnd() != null) {
            LocalDate end = p.getDateEnd().atZoneSameInstant(zone).toLocalDate();
            int available = workdaysBetween(start, end, noWeekends, blackout);
            if (total > available) { factor = available <= 0 ? 0 : (double) available / total; compressed = true; }
        }
        List<PaceScheduleDto.Step> steps = new ArrayList<>();
        int cumulative = 0;
        LocalDate last = start;
        for (CoursePaceItem i : items) {
            cumulative += i.getDurationDays() == null ? 0 : i.getDurationDays();
            int offset = compressed ? (int) Math.floor(cumulative * factor) : cumulative;
            LocalDate due = addWorkdays(start, offset, noWeekends, blackout);
            last = due;
            steps.add(new PaceScheduleDto.Step(i.getPaceItemId(), i.getModuleId(), i.getContentId() != null ? "CONTENT" : "ASSIGNMENT",
                    i.getContentId() != null ? i.getContentId() : i.getAssignmentId(), title(i), i.getDurationDays(), due));
        }
        return new PaceScheduleDto(p.getPaceId(), scope(p), userId, start, last, compressed, steps);
    }

    private static boolean workday(LocalDate d, boolean noWeekends, Set<LocalDate> blackout) {
        if (blackout.contains(d)) return false;
        return !noWeekends || (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY);
    }

    private static LocalDate firstWorkday(LocalDate d, boolean noWeekends, Set<LocalDate> blackout) {
        LocalDate x = d;
        for (int guard = 0; guard < 3660 && !workday(x, noWeekends, blackout); guard++) x = x.plusDays(1);
        return x;
    }

    private static LocalDate addWorkdays(LocalDate start, int days, boolean noWeekends, Set<LocalDate> blackout) {
        LocalDate x = start;
        for (int added = 0, guard = 0; added < days && guard < 3660; guard++) {
            x = x.plusDays(1);
            if (workday(x, noWeekends, blackout)) added++;
        }
        return x;
    }

    /** Working days after start up to and including end. */
    private static int workdaysBetween(LocalDate start, LocalDate end, boolean noWeekends, Set<LocalDate> blackout) {
        int n = 0;
        for (LocalDate x = start.plusDays(1); !x.isAfter(end) && n < 3660; x = x.plusDays(1)) if (workday(x, noWeekends, blackout)) n++;
        return n;
    }

    // ---------------------------------------------------------------- items

    /** Puts every content item and assignment of the course's modules into the pace, in module order. */
    private void sync(CoursePace p) {
        List<CoursePaceItem> existing = paceItems.findByPaceIdOrderByDisplayOrderAsc(p.getPaceId());
        Map<UUID, CoursePaceItem> byItem = new HashMap<>();
        existing.forEach(i -> byItem.put(i.getContentId() != null ? i.getContentId() : i.getAssignmentId(), i));
        Set<UUID> seen = new HashSet<>();
        int order = 0;
        for (CourseModule m : modules.findByCourseIdOrderByDisplayOrderAscModuleNumberAsc(p.getCourseId())) {
            record Step(UUID id, boolean content, int order, String title) {}
            List<Step> steps = new ArrayList<>();
            contents.findByModuleIdOrderByDisplayOrderAscContentTitleAsc(m.getModuleId())
                    .forEach(x -> steps.add(new Step(x.getContentId(), true, nz(x.getDisplayOrder()), x.getContentTitle())));
            assignments.findByModuleIdIn(List.of(m.getModuleId()))
                    .forEach(x -> steps.add(new Step(x.getAssignmentId(), false, nz(x.getDisplayOrder()), x.getAssignmentTitle())));
            steps.sort(Comparator.comparingInt(Step::order).thenComparing(Step::title));
            for (Step s : steps) {
                seen.add(s.id());
                CoursePaceItem i = byItem.get(s.id());
                if (i == null) {
                    i = new CoursePaceItem();
                    i.setPaceId(p.getPaceId());
                    i.setDurationDays(1);
                    if (s.content()) i.setContentId(s.id()); else i.setAssignmentId(s.id());
                }
                i.setModuleId(m.getModuleId());
                i.setDisplayOrder(order++);
                paceItems.save(i);
            }
        }
        for (CoursePaceItem i : existing) if (!seen.contains(i.getContentId() != null ? i.getContentId() : i.getAssignmentId())) paceItems.delete(i);
        paceItems.flush();
    }

    String title(CoursePaceItem i) {
        if (i.getContentId() != null) return contents.findById(i.getContentId()).map(Content::getContentTitle).orElse(null);
        return assignments.findById(i.getAssignmentId()).map(Assignment::getAssignmentTitle).orElse(null);
    }

    CoursePaceItemDto itemDto(CoursePaceItem i) {
        return new CoursePaceItemDto(i.getPaceItemId(), i.getPaceId(), i.getModuleId(),
                modules.findById(i.getModuleId()).map(CourseModule::getModuleTitle).orElse(null),
                i.getContentId() != null ? "CONTENT" : "ASSIGNMENT", i.getContentId() != null ? i.getContentId() : i.getAssignmentId(),
                title(i), i.getDurationDays(), i.getDisplayOrder());
    }

    // ---------------------------------------------------------------- helpers

    private static Set<LocalDate> blackouts(JsonNode n) {
        Set<LocalDate> out = new HashSet<>();
        if (n != null && n.isArray()) n.forEach(d -> out.add(LocalDate.parse(d.asText())));
        return out;
    }

    private static void validateBlackouts(JsonNode n) {
        if (n == null) return;
        if (!n.isArray()) throw ApiException.badRequest("INVALID_BLACKOUTS", "blackoutDates must be a JSON array of YYYY-MM-DD dates");
        for (JsonNode d : n) {
            try {
                LocalDate.parse(d.asText());
            } catch (DateTimeParseException e) {
                throw ApiException.badRequest("INVALID_BLACKOUTS", "Not a date: " + d.asText());
            }
        }
    }

    private static String scope(CoursePace p) {
        return p.getUserId() != null ? "STUDENT" : p.getSectionId() != null ? "SECTION" : "COURSE";
    }

    private static ZoneId zone(Course c) {
        try {
            return c.getTimeZone() == null ? ZoneId.systemDefault() : ZoneId.of(c.getTimeZone());
        } catch (Exception e) {
            return ZoneId.systemDefault();
        }
    }

    private CoursePace editable(UUID paceId) {
        CoursePace p = load(paceId);
        access.requireCourseEditor(p.getCourseId());
        if (p.getPaceStatus() == PaceStatus.ARCHIVED) throw ApiException.conflict("ARCHIVED", "The pace is archived");
        return p;
    }

    private void requireStaff(UUID courseId) {
        courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
        Access.requireTenant();
        if (!access.canSeeEverything() && !access.isCourseStaff(courseId)) throw ApiException.forbidden("Only course staff can see paces");
    }

    private CoursePace load(UUID id) {
        return paces.findById(id).orElseThrow(() -> ApiException.notFound("Pace"));
    }

    private CoursePaceDto toDto(CoursePace p) {
        return new CoursePaceDto(p.getPaceId(), p.getCourseId(), scope(p), p.getSectionId(), p.getUserId(), p.getPaceStatus(), p.getExcludeWeekends(),
                p.getBlackoutDates(), p.getDateEnd(), p.getDatePublished(),
                paceItems.findByPaceIdOrderByDisplayOrderAsc(p.getPaceId()).stream().map(this::itemDto).toList());
    }

    private static int nz(Integer i) {
        return i == null ? 0 : i;
    }
}
