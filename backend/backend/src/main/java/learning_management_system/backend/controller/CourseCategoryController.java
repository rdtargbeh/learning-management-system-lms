package learning_management_system.backend.controller;

import learning_management_system.backend.dto.CourseCategoryDto;
import learning_management_system.backend.dto.CreateCourseCategoryRequest;
import learning_management_system.backend.dto.UpdateCourseCategoryRequest;
import learning_management_system.backend.service.CourseCategoryService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Course catalog categories. */
@RestController
@RequestMapping("/api/v1/course-categories")
public class CourseCategoryController {
    private final CourseCategoryService categories;

    public CourseCategoryController(CourseCategoryService categories) { this.categories = categories; }

    @GetMapping
    public List<CourseCategoryDto> list() { return categories.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseCategoryDto create(@Valid @RequestBody CreateCourseCategoryRequest r) { return categories.create(r); }

    @PatchMapping("/{categoryId}")
    public CourseCategoryDto update(@PathVariable UUID categoryId, @Valid @RequestBody UpdateCourseCategoryRequest r) {
        return categories.update(categoryId, r);
    }
}
