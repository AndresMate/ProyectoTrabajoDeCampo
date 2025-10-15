package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.TournamentStatsDTO;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Tag(name = "Torneos", description = "Gestión de torneos deportivos")
public class TournamentController {

    private final TournamentService tournamentService;

    // === 🔓 PÚBLICO (sin autenticación) ===

    @Operation(summary = "Obtener torneos activos (PÚBLICO)")
    @GetMapping("/public/active")
    public ResponseEntity<List<TournamentResponseDTO>> getActiveTournaments() {
        List<TournamentResponseDTO> activeTournaments = tournamentService.findActiveTournaments();
        return ResponseEntity.ok(activeTournaments);
    }

    @Operation(summary = "Obtener torneo por ID (PÚBLICO)")
    @GetMapping("/public/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentPublic(@PathVariable Long id) {
        TournamentResponseDTO tournament = tournamentService.getById(id);
        return ResponseEntity.ok(tournament);
    }

    @Operation(summary = "Buscar torneos públicos (PÚBLICO)")
    @PostMapping("/public/search")
    public PageResponseDTO<TournamentResponseDTO> searchPublic(
            @RequestBody TournamentFilterDTO filter,
            Pageable pageable) {
        return tournamentService.search(filter, pageable); // Usar el mismo método existente
    }

    @Operation(summary = "Obtener todos los torneos ")
    @GetMapping("/public")
    public PageResponseDTO<TournamentResponseDTO> getAll(
            @Parameter(description = "Parámetros de paginación")
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return tournamentService.getAll(pageable);
    }

    // === 🔐 SOLO ADMIN Y SUPER_ADMIN (gestión completa) ===


    @Operation(summary = "Crear torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<TournamentResponseDTO> create(@RequestBody TournamentDTO dto) {
        TournamentResponseDTO created = tournamentService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public TournamentResponseDTO update(@PathVariable Long id, @RequestBody TournamentDTO dto) {
        return tournamentService.update(id, dto);
    }

    @Operation(summary = "Iniciar torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/start")
    public TournamentResponseDTO startTournament(@PathVariable Long id) {
        return tournamentService.startTournament(id);
    }

    @Operation(summary = "Completar torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/complete")
    public TournamentResponseDTO completeTournament(@PathVariable Long id) {
        return tournamentService.completeTournament(id);
    }

    @Operation(summary = "Cancelar torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/cancel")
    public TournamentResponseDTO cancelTournament(@PathVariable Long id) {
        return tournamentService.cancelTournament(id);
    }

    @Operation(summary = "Eliminar torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === 📊 ENDPOINTS DE CONSULTA PARA REFEREE ===

    @Operation(summary = "Obtener torneos en progreso (REFEREE)")
    @PreAuthorize("hasAnyRole('REFEREE', 'ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/in-progress")
    public ResponseEntity<List<TournamentResponseDTO>> getInProgressTournaments() {
        List<TournamentResponseDTO> tournaments = tournamentService.findInProgressTournaments();
        return ResponseEntity.ok(tournaments);
    }

    // === 🎯 ENDPOINTS ESPECÍFICOS PARA DASHBOARD ===

    @Operation(summary = "Estadísticas de torneos (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/stats")
    public ResponseEntity<TournamentStatsDTO> getTournamentStats() {
        TournamentStatsDTO stats = tournamentService.getTournamentStats();
        return ResponseEntity.ok(stats);
    }

}