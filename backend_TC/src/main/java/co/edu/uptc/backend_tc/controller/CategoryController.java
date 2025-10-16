package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.CategoryDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Operaciones sobre las categorías de los torneos")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Obtener todas las categorías paginadas", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @GetMapping
    public ResponseEntity<PageResponseDTO<CategoryDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @Operation(summary = "Obtener categorías activas por deporte", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @GetMapping("/sport/{sportId}/active")
    public ResponseEntity<List<CategoryDTO>> getActiveBySport(@PathVariable Long sportId) {
        return ResponseEntity.ok(categoryService.getActiveBySport(sportId));
    }

    @Operation(summary = "Obtener categoría por ID", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Operation(summary = "Crear una nueva categoría", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
        @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre para el deporte seleccionado")
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO dto) {
        CategoryDTO createdCategory = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @Operation(summary = "Actualizar una categoría existente", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto de nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @Operation(summary = "Desactivar una categoría (Soft Delete)", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Categoría desactivada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}