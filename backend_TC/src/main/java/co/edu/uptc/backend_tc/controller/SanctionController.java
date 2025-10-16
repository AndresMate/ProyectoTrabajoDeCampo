package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.dto.response.SanctionResponseDTO;
import co.edu.uptc.backend_tc.service.SanctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanctions")
@RequiredArgsConstructor
@Tag(name = "Sanciones", description = "Operaciones sobre sanciones a jugadores o equipos")
@SecurityRequirement(name = "bearerAuth")
public class SanctionController {

    private final SanctionService sanctionService;

    @Operation(summary = "Obtener sanciones por ID de equipo")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<SanctionResponseDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByTeam(teamId));
    }

    @Operation(summary = "Obtener sanciones por ID de jugador")
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SanctionResponseDTO>> getByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByPlayer(playerId));
    }

    @Operation(summary = "Obtener sanciones activas de un jugador")
    @GetMapping("/player/{playerId}/active")
    public ResponseEntity<List<SanctionResponseDTO>> getActiveByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(sanctionService.getActiveSanctionsByPlayer(playerId));
    }

    @Operation(summary = "Obtener sanciones por ID de partido")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SanctionResponseDTO>> getByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByMatch(matchId));
    }

    @Operation(summary = "Añadir una nueva sanción")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sanción añadida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Se debe especificar un jugador o un equipo"),
        @ApiResponse(responseCode = "404", description = "No se encontró el jugador, equipo o partido")
    })
    @PostMapping
    public ResponseEntity<SanctionDTO> addSanction(@RequestBody SanctionDTO dto) {
        SanctionDTO createdSanction = sanctionService.addSanction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSanction);
    }

    @Operation(summary = "Eliminar una sanción")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Sanción eliminada"),
        @ApiResponse(responseCode = "404", description = "Sanción no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanction(@PathVariable Long id) {
        sanctionService.deleteSanction(id);
        return ResponseEntity.noContent().build();
    }
}