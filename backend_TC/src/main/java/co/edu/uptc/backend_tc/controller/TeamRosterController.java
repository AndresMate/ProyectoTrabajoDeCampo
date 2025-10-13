package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.dto.response.TeamRosterResponseDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.TeamRosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-roster")
@RequiredArgsConstructor
@Tag(name = "Team Roster", description = "Operaciones sobre el roster de equipos")
public class TeamRosterController {

    private final TeamRosterService teamRosterService;

    @Operation(summary = "Obtener jugadores por equipo")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamRosterResponseDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamRosterService.getByTeam(teamId));
    }

    @Operation(summary = "Obtener equipos por jugador")
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<TeamRosterDTO>> getByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(teamRosterService.getByPlayer(playerId));
    }

    @Operation(summary = "Agregar jugador a equipo")
    @PostMapping
    public ResponseEntity<TeamRosterDTO> addPlayerToTeam(@RequestBody TeamRosterDTO dto) {
        return ResponseEntity.ok(teamRosterService.addPlayerToTeam(dto));
    }

    @Operation(summary = "Remover jugador de equipo")
    @DeleteMapping("/team/{teamId}/player/{playerId}")
    public ResponseEntity<Void> removePlayerFromTeam(@PathVariable Long teamId, @PathVariable Long playerId) {
        teamRosterService.removePlayerFromTeam(teamId, playerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Asignar capitán a equipo")
    @PutMapping("/team/{teamId}/captain/{playerId}")
    public ResponseEntity<TeamRosterDTO> setCaptain(@PathVariable Long teamId, @PathVariable Long playerId) {
        return ResponseEntity.ok(teamRosterService.setCaptain(teamId, playerId));
    }

    // Manejo de excepciones personalizadas
    @ExceptionHandler({ResourceNotFoundException.class, ConflictException.class, BusinessException.class})
    public ResponseEntity<String> handleCustomExceptions(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
