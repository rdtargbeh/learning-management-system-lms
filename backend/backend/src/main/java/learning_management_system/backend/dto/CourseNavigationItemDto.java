package learning_management_system.backend.dto;

import java.util.UUID;

/**
 * One entry of a course's navigation menu (table course_navigation_item, merged with the built-in tools). label is the
 * course's own label or the default; customised says the course changed it. Students never get hidden entries.
 */
public record CourseNavigationItemDto(String toolKey, String label, String defaultLabel, UUID externalToolId, Integer displayOrder,
                                      Boolean isHidden, boolean canHide, boolean customised) {}
