package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.TournamentStatsDTO;
import co.edu.uptc.backend_tc.service.TournamentService;
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
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Tag(name = "Torneos", description = "Operaciones sobre los torneos")
public class TournamentController {

    private final TournamentService tournamentService;

    // --- Endpoints Públicos ---

    @Operation(summary = "Obtener todos los torneos paginados (Público)")
    @GetMapping("/public")
    public ResponseEntity<PageResponseDTO<TournamentResponseDTO>> getAllPublic(Pageable pageable) {
        return ResponseEntity.ok(tournamentService.getAll(pageable));
    }

    @Operation(summary = "Buscar torneos por criterios (Público)")
    @PostMapping("/public/search")
    public ResponseEntity<PageResponseDTO<TournamentResponseDTO>> searchPublic(@RequestBody TournamentFilterDTO filter, Pageable pageable) {
        return ResponseEntity.ok(tournamentService.search(filter, pageable));
    }

    @Operation(summary = "Obtener un torneo por ID (Público)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Torneo encontrado"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    @GetMapping("/public/{id}")
    public ResponseEntity<TournamentResponseDTO> getByIdPublic(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @Operation(summary = "Obtener torneos activos (Público)")
    @GetMapping("/public/active")
    public ResponseEntity<List<TournamentResponseDTO>> findActiveTournaments() {
        return ResponseEntity.ok(tournamentService.findActiveTournaments());
    }

    // --- Endpoints Protegidos ---

    @Operation(summary = "Crear un nuevo torneo", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, como fechas incorrectas"),
        @ApiResponse(responseCode = "404", description = "Deporte o usuario creador no encontrado")
    })
    @PostMapping
    public ResponseEntity<TournamentResponseDTO> create(@RequestBody TournamentDTO dto) {
        TournamentResponseDTO createdTournament = tournamentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTournament);
    }

    @Operation(summary = "Actualizar un torneo", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> update(@PathVariable Long id, @RequestBody TournamentDTO dto) {
        return ResponseEntity.ok(tournamentService.update(id, dto));
    }

    @Operation(summary = "Iniciar un torneo", description = "Cambia el estado de PLANNING a IN_PROGRESS. Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/start")
    public ResponseEntity<TournamentResponseDTO> startTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.startTournament(id));
    }

    @Operation(summary = "Completar un torneo", description = "Cambia el estado de IN_PROGRESS a FINISHED. Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/complete")
    public ResponseEntity<TournamentResponseDTO> completeTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.completeTournament(id));
    }

    @Operation(summary = "Cancelar un torneo", description = "Cambia el estado a CANCELLED. Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TournamentResponseDTO> cancelTournament(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.cancelTournament(id));
    }

    @Operation(summary = "Eliminar un torneo", description = "Solo posible si está en PLANNING y no tiene partidos. Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener estadísticas de torneos", description = "Requiere rol ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stats")
    public ResponseEntity<TournamentStatsDTO> getTournamentStats() {
        return ResponseEntity.ok(tournamentService.getTournamentStats());
    }
}