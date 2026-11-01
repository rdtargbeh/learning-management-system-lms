package learning_management_system.backend.mapper;

import learning_management_system.backend.dto.CourseDto;
import learning_management_system.backend.entity.*;
import learning_management_system.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

//@Mapper(componentModel = "spring", uses = {StaffMapper.class, UserMapper.class})

@Component
public class CourseMapper {

    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    /** Convert Course entity to CourseDto.*/
    public CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }

        CourseDto dto = new CourseDto();
        dto.setCourseId(course.getCourseId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseTitle(course.getCourseTitle());
        dto.setCourseDescription(course.getCourseDescription());
        dto.setCourseStatus(course.getCourseStatus());
        dto.setPublished(course.getPublished());
        dto.setArchived(course.isArchived());
        dto.setTenantId(course.getTenant().getTenantId());
        dto.setDepartmentId(course.getDepartment().getDepartmentId());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setEnrollmentStart(course.getEnrollmentStart());
        dto.setEnrollmentEnd(course.getEnrollmentEnd());
        dto.setMaxEnrollments(course.getMaxEnrollments());
        dto.setVisibility(course.getVisibility());
        dto.setTags(course.getTags());
        dto.setCreatedBy(course.getCreatedBy() != null ? course.getCreatedBy().getUserId() : null);
        dto.setTenantId(course.getTenant() != null ? course.getTenant().getTenantId() : null);
        dto.setDepartmentId(course.getDepartment() != null ? course.getDepartment().getDepartmentId() : null);
        dto.setStudentIds(course.getStudents() != null ? course.getStudents().stream()
                .map(User::getUserId)
                .collect(Collectors.toSet()) : null);
        dto.setStaffIds(course.getStaff().stream().map(Staff::getStaffId).collect(Collectors.toSet()));
        dto.setDateCreated(course.getDateCreated());
        dto.setDateUpdated(course.getDateUpdated());
        dto.setMetadata(course.getMetadata());
        return dto;
    }

    /**
     * Convert CourseDto to Course entity.
     */
    public Course toEntity(CourseDto dto) {
        if (dto == null) {
            return null;
        }

        Course course = new Course();
        course.setCourseId(dto.getCourseId());
        course.setCourseCode(dto.getCourseCode());
        course.setCourseTitle(dto.getCourseTitle());
        course.setCourseDescription(dto.getCourseDescription());
        course.setCourseStatus(dto.getCourseStatus());
        course.setPublished(dto.getPublished());
        course.setArchived(dto.isArchived());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setEnrollmentStart(dto.getEnrollmentStart());
        course.setEnrollmentEnd(dto.getEnrollmentEnd());
        course.setMaxEnrollments(dto.getMaxEnrollments());
        course.setVisibility(dto.getVisibility());
        course.setTags(dto.getTags());
        course.setDateCreated(dto.getDateCreated());
        course.setDateUpdated(dto.getDateUpdated());
        course.setMetadata(dto.getMetadata());

        if (dto.getCreatedBy() != null) {
            User createdBy = userRepository.findById(dto.getCreatedBy())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + dto.getCreatedBy()));
            course.setCreatedBy(createdBy);
        }

        // Resolve and set staff
        if (dto.getStaffIds() != null) {
            Set<Staff> staff = staffRepository.findAllById(dto.getStaffIds())
                    .stream()
                    .collect(Collectors.toSet());
            course.setStaff(staff);
        }

        // Fetch and set students
        if (dto.getStudentIds() != null) {
            Set<User> students = userRepository.findAllById(dto.getStudentIds())
                    .stream()
                    .collect(Collectors.toSet());
            course.setStudents(students);
        }

        // Fetch and set tenant
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + dto.getTenantId()));
            course.setTenant(tenant);
        }

        // Fetch and set department
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + dto.getDepartmentId()));
            course.setDepartment(department);
        }

        return course;
    }


}
