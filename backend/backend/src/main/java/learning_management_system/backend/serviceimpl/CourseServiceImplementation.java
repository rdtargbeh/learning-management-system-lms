package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.AssignCourseInstructorRequest;
import learning_management_system.backend.dto.CourseDto;
import learning_management_system.backend.dto.CourseSectionDto;
import learning_management_system.backend.dto.CreateCourseRequest;
import learning_management_system.backend.dto.CreateCourseSectionRequest;
import learning_management_system.backend.dto.MyCourseDto;
import learning_management_system.backend.dto.PageResponse;
import learning_management_system.backend.dto.SelfEnrollmentSettingsRequest;
import learning_management_system.backend.dto.UpdateCourseRequest;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseInstructor;
import learning_management_system.backend.entity.CourseSection;
import learning_management_system.backend.entity.Enrollment;
import learning_management_system.backend.enums.CourseInstructorRole;
import learning_management_system.backend.enums.CourseStatus;
import learning_management_system.backend.enums.EnrollmentStatus;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.mapper.CourseMapper;
import learning_management_system.backend.repository.AcademicTermRepository;
import learning_management_system.backend.repository.CourseCategoryRepository;
import learning_management_system.backend.repository.CourseInstructorRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.CourseSectionRepository;
import learning_management_system.backend.repository.EnrollmentRepository;
import learning_management_system.backend.repository.OrgUnitRepository;
import learning_management_system.backend.security.Access;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.security.Permissions;
import learning_management_system.backend.service.CourseSectionService;
import learning_management_system.backend.service.CourseService;
import learning_management_system.backend.utility.EntityRefresher;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseServiceImplementation implements CourseService {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";   // no 0/O or 1/I look-alikes
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CourseRepository courses;
    private final CourseSectionRepository sections;
    private final CourseInstructorRepository staff;
    private final EnrollmentRepository enrollments;
    private final AcademicTermRepository terms;
    private final CourseCategoryRepository categories;
    private final OrgUnitRepository orgUnits;
    private final CourseSectionService sectionService;
    private final CourseAccess access;
    private final CourseMapper courseMapper;
    private final EntityRefresher refresher;

    public CourseServiceImplementation(CourseRepository courses, CourseSectionRepository sections,
                                       CourseInstructorRepository staff, EnrollmentRepository enrollments,
                                       AcademicTermRepository terms, CourseCategoryRepository categories,
                                       OrgUnitRepository orgUnits, CourseSectionService sectionService, CourseAccess access,
                                       CourseMapper courseMapper, EntityRefresher refresher) {
        this.courses = courses;
        this.sections = sections;
        this.staff = staff;
        this.enrollments = enrollments;
        this.terms = terms;
        this.categories = categories;
        this.orgUnits = orgUnits;
        this.sectionService = sectionService;
        this.access = access;
        this.courseMapper = courseMapper;
        this.refresher = refresher;
    }

    @Transactional
    @Override
    public CourseDto create(CreateCourseRequest r) {
        access.requireCourseAdmin();
        validateReferences(r.termId(), r.categoryId(), r.orgUnitId());
        boolean defaultSection = r.createDefaultSection() == null || r.createDefaultSection();
        if (r.instructorUserId() != null && !defaultSection) {
            throw ApiException.badRequest("SECTION_REQUIRED", "instructorUserId needs the default section");
        }
        Course c = courseMapper.toEntity(r);
        c.setCourseCode(r.courseCode().trim());
        c.setCourseTitle(r.courseTitle().trim());
        validateDates(c);
        refresher.refresh(courses.saveAndFlush(c));
        if (defaultSection) {
            CourseSectionDto s = sectionService.create(c.getCourseId(), new CreateCourseSectionRequest(
                    "01", null, null, r.defaultSectionCapacity(), null, null, null, null, null, null));
            if (r.instructorUserId() != null) {
                sectionService.assignStaff(s.sectionId(),
                        new AssignCourseInstructorRequest(r.instructorUserId(), CourseInstructorRole.PRIMARY, null, null));
            }
        }
        return courseMapper.toDto(c);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<CourseDto> search(UUID termId, CourseStatus status, UUID categoryId, UUID orgUnitId, String q,
                                          int page, int size) {
        Access.requireTenant();
        if (!access.canSeeEverything()) throw ApiException.forbidden("Missing permission " + Permissions.COURSE_READ);
        return PageResponse.of(courses.search(termId, status, categoryId, orgUnitId, q == null || q.isBlank() ? null : q.trim(),
                PageRequest.of(page, Math.min(size, 200), Sort.by("courseCode"))), courseMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MyCourseDto> mine() {
        Access.requireTenant();
        UUID me = Access.ctx().userId();
        LocalDate today = LocalDate.now();
        List<CourseInstructor> teaching = staff.findByUserId(me).stream()
                .filter(ci -> ci.getDateEnd() == null || !ci.getDateEnd().isBefore(today)).toList();
        List<Enrollment> enrolled = enrollments.findByUserIdOrderByDateEnrolledDesc(me).stream()
                .filter(e -> e.getEnrollmentStatus() == EnrollmentStatus.ACTIVE).toList();
        List<UUID> sectionIds = new ArrayList<>();
        teaching.forEach(ci -> sectionIds.add(ci.getSectionId()));
        enrolled.forEach(e -> sectionIds.add(e.getSectionId()));
        Map<UUID, CourseSection> sectionById = sections.findAllById(sectionIds).stream()
                .collect(Collectors.toMap(CourseSection::getSectionId, Function.identity()));
        Map<UUID, Course> courseById = courses.findAllById(sectionById.values().stream().map(CourseSection::getCourseId).toList())
                .stream().collect(Collectors.toMap(Course::getCourseId, Function.identity()));

        List<MyCourseDto> out = new ArrayList<>();
        Map<UUID, Boolean> listed = new HashMap<>();
        for (CourseInstructor ci : teaching) {
            CourseSection s = sectionById.get(ci.getSectionId());
            Course c = s == null ? null : courseById.get(s.getCourseId());
            if (c == null || Boolean.TRUE.equals(c.getIsArchived())) continue;
            out.add(new MyCourseDto(courseMapper.toDto(c), s.getSectionId(), s.getSectionNumber(), ci.getRole().name(), null));
            listed.put(s.getSectionId(), true);
        }
        for (Enrollment e : enrolled) {
            CourseSection s = sectionById.get(e.getSectionId());
            Course c = s == null ? null : courseById.get(s.getCourseId());
            if (c == null || Boolean.TRUE.equals(c.getIsArchived()) || listed.containsKey(s.getSectionId())) continue;
            if (c.getCourseStatus() != CourseStatus.PUBLISHED && c.getCourseStatus() != CourseStatus.CONCLUDED) continue;
            out.add(new MyCourseDto(courseMapper.toStudentDto(c), s.getSectionId(), s.getSectionNumber(),
                    e.getEnrollmentType().name(), e.getEnrollmentStatus()));
        }
        out.sort(Comparator.comparing((MyCourseDto m) -> m.course().courseCode()).thenComparing(m -> m.sectionNumber()));
        return out;
    }

    @Transactional(readOnly = true)
    @Override
    public CourseDto get(UUID courseId) {
        Course c = load(courseId);
        access.requireCourseViewer(c);
        return view(c);
    }

    @Transactional
    @Override
    public CourseDto update(UUID courseId, UpdateCourseRequest r) {
        Course c = load(courseId);
        access.requireCourseEditor(courseId);
        boolean catalogChange = r.courseCode() != null || r.termId() != null || r.categoryId() != null || r.orgUnitId() != null
                || r.sisSourceId() != null || r.creditHours() != null || r.totalSeats() != null || r.courseKind() != null;
        if (catalogChange && !Access.hasTenantPermission(Permissions.COURSE_MANAGE)) {
            throw ApiException.forbidden("Code, term, category, org unit, SIS id, credits, seats and kind need " + Permissions.COURSE_MANAGE);
        }
        validateReferences(r.termId(), r.categoryId(), r.orgUnitId());
        courseMapper.updateEntity(r, c);
        validateDates(c);
        return view(courses.saveAndFlush(c));
    }

    @Transactional
    @Override
    public CourseDto changeStatus(UUID courseId, CourseStatus status) {
        Course c = load(courseId);
        boolean archiving = status == CourseStatus.ARCHIVED || c.getCourseStatus() == CourseStatus.ARCHIVED;
        if (archiving) access.requireCourseAdmin();
        else access.requireCourseEditor(courseId);
        c.setCourseStatus(status);
        c.setIsArchived(status == CourseStatus.ARCHIVED);
        if (status == CourseStatus.ARCHIVED) {          // nobody can join an archived course
            c.setAllowSelfEnrollment(false);
            c.setSelfEnrollmentCode(null);
        }
        return view(courses.saveAndFlush(c));
    }

    @Transactional
    @Override
    public CourseDto selfEnrollment(UUID courseId, SelfEnrollmentSettingsRequest r) {
        Course c = load(courseId);
        access.requireCourseEditor(courseId);
        if (r.enabled()) {
            if (Boolean.TRUE.equals(c.getIsArchived())) throw ApiException.conflict("COURSE_ARCHIVED", "The course is archived");
            c.setAllowSelfEnrollment(true);
            if (c.getSelfEnrollmentCode() == null || Boolean.TRUE.equals(r.regenerateCode())) c.setSelfEnrollmentCode(newCode());
        } else {
            c.setAllowSelfEnrollment(false);
            c.setSelfEnrollmentCode(null);
        }
        return view(courses.saveAndFlush(c));
    }

    // ---------------------------------------------------------------- helpers

    /** Staff and administrators see the self-enrollment code; students do not. */
    private CourseDto view(Course c) {
        return access.canSeeEverything() || access.isCourseStaff(c.getCourseId()) ? courseMapper.toDto(c) : courseMapper.toStudentDto(c);
    }

    private String newCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            if (courses.findBySelfEnrollmentCode(sb.toString()).isEmpty()) return sb.toString();
        }
        throw new IllegalStateException("Could not generate a unique self-enrollment code");
    }

    private void validateReferences(UUID termId, UUID categoryId, UUID orgUnitId) {
        if (termId != null && terms.findById(termId).isEmpty()) throw ApiException.badRequest("UNKNOWN_TERM", "Unknown term");
        if (categoryId != null && categories.findById(categoryId).isEmpty()) {
            throw ApiException.badRequest("UNKNOWN_CATEGORY", "Unknown course category");
        }
        if (orgUnitId != null && orgUnits.findById(orgUnitId).isEmpty()) throw ApiException.badRequest("UNKNOWN_ORG_UNIT", "Unknown org unit");
    }

    private static void validateDates(Course c) {
        if (c.getDateStart() != null && c.getDateEnd() != null && c.getDateEnd().isBefore(c.getDateStart())) {
            throw ApiException.badRequest("INVALID_DATES", "dateEnd must be on or after dateStart");
        }
    }

    private Course load(UUID courseId) {
        return courses.findById(courseId).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
