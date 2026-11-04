package learning_management_system.backend.serviceimpl;

import learning_management_system.backend.dto.CourseNavigationItemDto;
import learning_management_system.backend.dto.UpdateCourseNavigationRequest;
import learning_management_system.backend.entity.Course;
import learning_management_system.backend.entity.CourseNavigationItem;
import learning_management_system.backend.entity.ExternalTool;
import learning_management_system.backend.exception.ApiException;
import learning_management_system.backend.repository.CourseNavigationItemRepository;
import learning_management_system.backend.repository.CourseRepository;
import learning_management_system.backend.repository.ExternalToolRepository;
import learning_management_system.backend.security.CourseAccess;
import learning_management_system.backend.service.CourseNavigationItemService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseNavigationItemServiceImplementation implements CourseNavigationItemService {
    /** Built-in tools in their default order, with default labels. */
    static final LinkedHashMap<String, String> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("HOME", "Home");
        DEFAULTS.put("ANNOUNCEMENTS", "Announcements");
        DEFAULTS.put("MODULES", "Modules");
        DEFAULTS.put("ASSIGNMENTS", "Assignments");
        DEFAULTS.put("QUIZZES", "Quizzes");
        DEFAULTS.put("DISCUSSIONS", "Discussions");
        DEFAULTS.put("GRADES", "Grades");
        DEFAULTS.put("PEOPLE", "People");
        DEFAULTS.put("PAGES", "Pages");
        DEFAULTS.put("FILES", "Files");
        DEFAULTS.put("SYLLABUS", "Syllabus");
        DEFAULTS.put("OUTCOMES", "Outcomes");
        DEFAULTS.put("CALENDAR", "Calendar");
        DEFAULTS.put("COLLABORATIONS", "Collaborations");
        DEFAULTS.put("CONFERENCES", "Conferences");
    }
    static final String EXTERNAL = "EXTERNAL_TOOL";
    private static final String SETTINGS = "SETTINGS";

    private final CourseNavigationItemRepository items;
    private final CourseRepository courses;
    private final ExternalToolRepository tools;
    private final CourseAccess access;

    public CourseNavigationItemServiceImplementation(CourseNavigationItemRepository items, CourseRepository courses,
                                                     ExternalToolRepository tools, CourseAccess access) {
        this.items = items;
        this.courses = courses;
        this.tools = tools;
        this.access = access;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CourseNavigationItemDto> list(UUID courseId) {
        Course c = course(courseId);
        access.requireCourseViewer(c);
        boolean staff = access.canSeeEverything() || access.isCourseStaff(courseId);
        List<CourseNavigationItemDto> all = merged(courseId);
        return staff ? all : all.stream().filter(i -> !Boolean.TRUE.equals(i.isHidden()) && !SETTINGS.equals(i.toolKey())).toList();
    }

    @Transactional
    @Override
    public List<CourseNavigationItemDto> replace(UUID courseId, UpdateCourseNavigationRequest r) {
        course(courseId);
        access.requireCourseEditor(courseId);
        Set<String> seen = new HashSet<>();
        List<CourseNavigationItem> rows = new ArrayList<>();
        int order = 0;
        for (UpdateCourseNavigationRequest.Item i : r.items()) {
            String key = i.toolKey().trim().toUpperCase(Locale.ROOT);
            if (SETTINGS.equals(key)) throw ApiException.badRequest("SETTINGS_FIXED", "Settings is always the last entry and cannot be changed");
            UUID toolId = null;
            if (EXTERNAL.equals(key)) {
                if (i.externalToolId() == null) throw ApiException.badRequest("TOOL_REQUIRED", "EXTERNAL_TOOL entries need externalToolId");
                ExternalTool t = tools.findById(i.externalToolId()).filter(x -> !Boolean.FALSE.equals(x.getIsActive()))
                        .orElseThrow(() -> ApiException.badRequest("UNKNOWN_TOOL", "Unknown or inactive external tool"));
                toolId = t.getToolId();
                key = EXTERNAL + ":" + toolId;
            } else if (!DEFAULTS.containsKey(key)) {
                throw ApiException.badRequest("UNKNOWN_TOOL_KEY", "Unknown navigation entry " + i.toolKey());
            }
            if (!seen.add(key)) throw ApiException.badRequest("DUPLICATE_ENTRY", "Each entry may appear once: " + key);
            if ("HOME".equals(key) && Boolean.TRUE.equals(i.isHidden())) throw ApiException.badRequest("HOME_REQUIRED", "Home cannot be hidden");
            CourseNavigationItem n = new CourseNavigationItem();
            n.setCourseId(courseId);
            n.setToolKey(key);
            n.setExternalToolId(toolId);
            n.setLabel(i.label() == null || i.label().isBlank() ? null : i.label().trim());
            n.setIsHidden(Boolean.TRUE.equals(i.isHidden()));
            n.setDisplayOrder(order++);
            rows.add(n);
        }
        items.deleteByCourse(courseId);
        items.saveAllAndFlush(rows);
        return merged(courseId);
    }

    /** Saved entries first in their order, then the built-in tools the course has not placed, then Settings. */
    private List<CourseNavigationItemDto> merged(UUID courseId) {
        List<CourseNavigationItemDto> out = new ArrayList<>();
        Set<String> placed = new HashSet<>();
        int order = 0;
        for (CourseNavigationItem n : items.findByCourseIdOrderByDisplayOrderAsc(courseId)) {
            String def = n.getExternalToolId() != null
                    ? tools.findById(n.getExternalToolId()).map(ExternalTool::getToolName).orElse("External tool")
                    : DEFAULTS.getOrDefault(n.getToolKey(), n.getToolKey());
            placed.add(n.getToolKey());
            out.add(new CourseNavigationItemDto(n.getToolKey(), n.getLabel() != null ? n.getLabel() : def, def, n.getExternalToolId(), order++,
                    n.getIsHidden(), !"HOME".equals(n.getToolKey()), true));
        }
        for (Map.Entry<String, String> d : DEFAULTS.entrySet()) {
            if (placed.contains(d.getKey())) continue;
            out.add(new CourseNavigationItemDto(d.getKey(), d.getValue(), d.getValue(), null, order++, false, !"HOME".equals(d.getKey()), false));
        }
        out.add(new CourseNavigationItemDto(SETTINGS, "Settings", "Settings", null, order, false, false, false));
        return out;
    }

    private Course course(UUID id) {
        return courses.findById(id).orElseThrow(() -> ApiException.notFound("Course"));
    }
}
