package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.SportService;
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
@RequestMapping("/api/sports")
@RequiredArgsConstructor
@Tag(name = "Deportes", description = "Operaciones sobre los deportes")
public class SportController {

    private final SportService sportService;

    // --- Endpoints públicos ---
    @Operation(summary = "Obtener todos los deportes activos (Público)")
    @GetMapping("/public/active")
    public ResponseEntity<List<SportDTO>> getAllActive() {
        return ResponseEntity.ok(sportService.getAllActive());
    }

    // --- Endpoints protegidos ---
    @Operation(summary = "Obtener todos los deportes paginados (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<PageResponseDTO<SportDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(sportService.getAll(pageable));
    }

    @Operation(summary = "Obtener un deporte por ID (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deporte encontrado"),
        @ApiResponse(responseCode = "404", description = "Deporte no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SportDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sportService.getById(id));
    }

    @Operation(summary = "Crear un nuevo deporte (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Deporte creado"),
        @ApiResponse(responseCode = "409", description = "Ya existe un deporte con ese nombre")
    })
    @PostMapping
    public ResponseEntity<SportDTO> create(@RequestBody SportDTO dto) {
        SportDTO createdSport = sportService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSport);
    }

    @Operation(summary = "Actualizar un deporte (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deporte actualizado"),
        @ApiResponse(responseCode = "404", description = "Deporte no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto de nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SportDTO> update(@PathVariable Long id, @RequestBody SportDTO dto) {
        return ResponseEntity.ok(sportService.update(id, dto));
    }

    @Operation(summary = "Desactivar un deporte (Soft Delete) (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deporte desactivado"),
        @ApiResponse(responseCode = "404", description = "Deporte no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}