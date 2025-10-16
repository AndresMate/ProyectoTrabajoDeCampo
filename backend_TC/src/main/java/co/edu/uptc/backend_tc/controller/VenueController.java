package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.VenueService;
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
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Sedes (Venues)", description = "Operaciones sobre las sedes de los eventos deportivos")
public class VenueController {

    private final VenueService venueService;

    // --- Endpoints Públicos ---

    @Operation(summary = "Buscar sedes por nombre (Público)")
    @GetMapping("/public/search")
    public ResponseEntity<List<VenueDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(venueService.searchByName(name));
    }

    // --- Endpoints Protegidos ---

    @Operation(summary = "Obtener todas las sedes paginadas", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<PageResponseDTO<VenueDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(venueService.getAll(pageable));
    }

    @Operation(summary = "Obtener una sede por ID", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sede encontrada"),
        @ApiResponse(responseCode = "404", description = "Sede no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VenueDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getById(id));
    }

    @Operation(summary = "Crear una nueva sede", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sede creada"),
        @ApiResponse(responseCode = "409", description = "Ya existe una sede con ese nombre")
    })
    @PostMapping
    public ResponseEntity<VenueDTO> create(@RequestBody VenueDTO dto) {
        VenueDTO createdVenue = venueService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    }

    @Operation(summary = "Actualizar una sede", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sede actualizada"),
        @ApiResponse(responseCode = "404", description = "Sede no encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflicto de nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VenueDTO> update(@PathVariable Long id, @RequestBody VenueDTO dto) {
        return ResponseEntity.ok(venueService.update(id, dto));
    }

    @Operation(summary = "Eliminar una sede", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sede eliminada"),
        @ApiResponse(responseCode = "404", description = "Sede no encontrada"),
        @ApiResponse(responseCode = "400", description = "La sede tiene escenarios asociados y no puede ser eliminada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}