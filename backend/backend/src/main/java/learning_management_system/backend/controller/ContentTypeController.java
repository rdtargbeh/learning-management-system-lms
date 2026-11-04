package learning_management_system.backend.controller;

import learning_management_system.backend.dto.ContentTypeDto;
import learning_management_system.backend.dto.CreateContentTypeRequest;
import learning_management_system.backend.service.ContentTypeService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Content types (PAGE, VIDEO, FILE, LINK ... plus institution-specific ones). */
@RestController
@RequestMapping("/api/v1/content-types")
public class ContentTypeController {
    private final ContentTypeService types;

    public ContentTypeController(ContentTypeService types) { this.types = types; }

    @GetMapping
    public List<ContentTypeDto> list() { return types.list(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContentTypeDto create(@Valid @RequestBody CreateContentTypeRequest r) { return types.create(r); }
}
