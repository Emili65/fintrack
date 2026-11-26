package com.fintrack.category;

import com.fintrack.category.dto.CategoryRequest;
import com.fintrack.category.dto.CategoryResponse;
import com.fintrack.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías, opcionalmente filtradas por tipo")
    public List<CategoryResponse> list(@AuthenticationPrincipal AuthUser user,
                                       @RequestParam(required = false) CategoryType type) {
        return categoryService.findAll(user.id(), type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una categoría")
    public CategoryResponse create(@AuthenticationPrincipal AuthUser user,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(user.id(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría")
    public CategoryResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(user.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una categoría sin transacciones")
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        categoryService.delete(user.id(), id);
    }
}
