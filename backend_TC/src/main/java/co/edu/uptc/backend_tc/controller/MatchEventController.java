package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.dto.response.MatchEventResponseDTO;
import co.edu.uptc.backend_tc.service.MatchEventService;
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
@RequestMapping("/api/match-events")
@RequiredArgsConstructor
@Tag(name = "Eventos de Partido", description = "Operaciones sobre los eventos de un partido (goles, tarjetas, etc.)")
@SecurityRequirement(name = "bearerAuth")
public class MatchEventController {

    private final MatchEventService matchEventService;

    @Operation(summary = "Obtener eventos por ID de partido", description = "Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MatchEventResponseDTO>> getByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchEventService.getByMatch(matchId));
    }

    @Operation(summary = "Obtener eventos por ID de jugador", description = "Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchEventDTO>> getByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(matchEventService.getByPlayer(playerId));
    }

    @Operation(summary = "Crear un nuevo evento de partido", description = "Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o el jugador no pertenece al partido"),
            @ApiResponse(responseCode = "404", description = "Partido o jugador no encontrado")
    })
    @PostMapping
    public ResponseEntity<MatchEventDTO> create(@RequestBody MatchEventDTO dto) {
        MatchEventDTO createdEvent = matchEventService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @Operation(summary = "Eliminar un evento de partido", description = "Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
