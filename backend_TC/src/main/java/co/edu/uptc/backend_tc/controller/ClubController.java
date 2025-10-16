package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.ClubService;
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
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubes", description = "Operaciones sobre los clubes deportivos")
@SecurityRequirement(name = "bearerAuth")
public class ClubController {

    private final ClubService clubService;

    @Operation(summary = "Obtener todos los clubes paginados")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ClubDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(clubService.getAll(pageable));
    }

    @Operation(summary = "Obtener todos los clubes activos")
    @GetMapping("/active")
    public ResponseEntity<List<ClubDTO>> getAllActive() {
        return ResponseEntity.ok(clubService.getAllActive());
    }

    @Operation(summary = "Obtener un club por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Club encontrado"),
        @ApiResponse(responseCode = "404", description = "Club no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getById(id));
    }

    @Operation(summary = "Crear un nuevo club")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Club creado exitosamente"),
        @ApiResponse(responseCode = "409", description = "Ya existe un club con ese nombre")
    })
    @PostMapping
    public ResponseEntity<ClubDTO> create(@RequestBody ClubDTO dto) {
        ClubDTO createdClub = clubService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClub);
    }

    @Operation(summary = "Actualizar un club existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Club actualizado"),
        @ApiResponse(responseCode = "404", description = "Club no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto de nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClubDTO> update(@PathVariable Long id, @RequestBody ClubDTO dto) {
        return ResponseEntity.ok(clubService.update(id, dto));
    }

    @Operation(summary = "Desactivar un club (Soft Delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Club desactivado"),
        @ApiResponse(responseCode = "404", description = "Club no encontrado"),
        @ApiResponse(responseCode = "400", description = "El club tiene equipos activos y no puede ser eliminado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubService.delete(id);
        return ResponseEntity.noContent().build();
    }
}