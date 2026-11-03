package learning_management_system.backend.controller;

import learning_management_system.backend.dto.AssociateBlueprintCoursesRequest;
import learning_management_system.backend.dto.BlueprintAssociationDto;
import learning_management_system.backend.dto.BlueprintStatusDto;
import learning_management_system.backend.service.BlueprintAssociationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** blueprint_association: a blueprint's associated courses, and an associated course's blueprint status. */
@RestController
@RequestMapping("/api/v1/courses/{courseId}")
public class BlueprintAssociationController {
    private final BlueprintAssociationService associations;

    public BlueprintAssociationController(BlueprintAssociationService associations) { this.associations = associations; }

    @GetMapping("/blueprint/associations")
    public List<BlueprintAssociationDto> list(@PathVariable UUID courseId) { return associations.list(courseId); }

    @PostMapping("/blueprint/associations")
    @ResponseStatus(HttpStatus.CREATED)
    public List<BlueprintAssociationDto> associate(@PathVariable UUID courseId, @Valid @RequestBody AssociateBlueprintCoursesRequest r) {
        return associations.associate(courseId, r);
    }

    @DeleteMapping("/blueprint/associations/{associationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID courseId, @PathVariable UUID associationId) { associations.remove(courseId, associationId); }

    /** For an associated course: which blueprint it follows and what is locked. */
    @GetMapping("/blueprint-status")
    public BlueprintStatusDto status(@PathVariable UUID courseId) { return associations.status(courseId); }
}
