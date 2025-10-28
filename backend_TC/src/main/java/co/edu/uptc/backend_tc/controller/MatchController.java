package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    // ================================
    // ENDPOINTS PÚBLICOS
    // ================================

    /**
     * Obtener todos los partidos (público)
     */
    @GetMapping("/public")
    public ResponseEntity<List<MatchResponseDTO>> getAllMatchesPublic(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) Long categoryId
    ) {
        List<MatchResponseDTO> matches;

        if (tournamentId != null && categoryId != null) {
            // ✅ Filtrar por torneo Y categoría
            matches = matchService.getMatchesByTournamentAndCategory(tournamentId, categoryId);
        } else if (tournamentId != null) {
            // ✅ Filtrar solo por torneo
            matches = matchService.getMatchesByTournament(tournamentId);
        } else {
            // Sin filtros, devolver todos
            matches = matchService.getAllMatches();
        }

        return ResponseEntity.ok(matches);
    }

    /**
     * Obtener partido por ID (público)
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<MatchResponseDTO> getMatchByIdPublic(@PathVariable Long id) {
        MatchResponseDTO match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    // ================================
    // ENDPOINTS PROTEGIDOS (Admin/Referee)
    // ================================

    /**
     * Crear nuevo partido
     * Requiere rol ADMIN o REFEREE
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'REFEREE')")
    public ResponseEntity<MatchDTO> createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        MatchDTO created = matchService.createMatch(matchDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar partido existente
     * Requiere rol ADMIN o REFEREE
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'REFEREE')")
    public ResponseEntity<MatchResponseDTO> updateMatch(
            @PathVariable Long id,
            @Valid @RequestBody MatchDTO matchDTO
    ) {
        MatchResponseDTO updated = matchService.updateMatch(id, matchDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Eliminar partido
     * Requiere rol ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Iniciar partido (cambiar estado a IN_PROGRESS)
     * Requiere rol REFEREE o ADMIN
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'REFEREE')")
    public ResponseEntity<MatchResponseDTO> startMatch(@PathVariable Long id) {
        MatchResponseDTO match = matchService.startMatch(id);
        return ResponseEntity.ok(match);
    }

    /**
     * Finalizar partido (cambiar estado a FINISHED)
     * Requiere rol REFEREE o ADMIN
     */
    @PostMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'REFEREE')")
    public ResponseEntity<MatchResponseDTO> finishMatch(@PathVariable Long id) {
        MatchResponseDTO match = matchService.finishMatch(id);
        return ResponseEntity.ok(match);
    }

    // ================================
    // ENDPOINTS ADICIONALES
    // ================================

    /**
     * Obtener partidos de un torneo específico
     * Endpoint dedicado para mayor claridad
     */
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<MatchResponseDTO> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    /**
     * Obtener partidos de un torneo y categoría específicos
     */
    @GetMapping("/tournament/{tournamentId}/category/{categoryId}")
    public ResponseEntity<List<MatchResponseDTO>> getMatchesByTournamentAndCategory(
            @PathVariable Long tournamentId,
            @PathVariable Long categoryId
    ) {
        List<MatchResponseDTO> matches = matchService.getMatchesByTournamentAndCategory(tournamentId, categoryId);
        return ResponseEntity.ok(matches);
    }
}