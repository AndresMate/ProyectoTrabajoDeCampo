package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.StandingDTO;
import co.edu.uptc.backend_tc.dto.response.StandingResponseDTO;
import co.edu.uptc.backend_tc.service.StandingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
@Tag(name = "Standings", description = "Operaciones sobre tablas de posiciones")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*")
public class StandingController {

    private final StandingService standingService;

    @Operation(summary = "Obtener standings por torneo y categoría")
    @GetMapping("/tournament/{tournamentId}/category/{categoryId}")
    public ResponseEntity<List<StandingResponseDTO>> getStandingsWithPosition(
            @PathVariable Long tournamentId,
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(standingService.getStandingsWithPosition(tournamentId, categoryId));
    }

    @Operation(summary = "Recalcular standings desde los resultados de partidos")
    @PostMapping("/recalculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> recalculateStandings(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        standingService.recalculateFromResults(tournamentId, categoryId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Standings recalculados exitosamente desde los resultados de partidos");
        return ResponseEntity.ok(response);
    }
}