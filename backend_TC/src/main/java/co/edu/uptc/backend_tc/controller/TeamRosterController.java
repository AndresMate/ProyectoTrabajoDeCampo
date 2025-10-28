package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.service.TeamService;
import co.edu.uptc.backend_tc.service.TeamRosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/roster")
@RequiredArgsConstructor
@Tag(name = "Equipos - Roster", description = "Operaciones para gestionar los jugadores de un equipo")
@SecurityRequirement(name = "bearerAuth")
public class TeamRosterController {

    private final TeamRosterService teamRosterService;
    private final TeamService teamService;

    // ✅ AHORA devuelve el equipo completo con club, torneo y roster
    @Operation(summary = "Obtener el roster completo de un equipo con datos del club y torneo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roster obtenido correctamente"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @GetMapping
    public ResponseEntity<TeamResponseDTO> getRosterByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeamRoster(teamId));
    }

    @Operation(summary = "Añadir un jugador al roster del equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jugador añadido al roster"),
            @ApiResponse(responseCode = "409", description = "El jugador ya está en el equipo o el número de camiseta está ocupado"),
            @ApiResponse(responseCode = "404", description = "Equipo o jugador no encontrado"),
            @ApiResponse(responseCode = "400", description = "Se alcanzó el límite de jugadores")
    })
    @PostMapping
    public ResponseEntity<TeamRosterDTO> addPlayerToTeam(@PathVariable Long teamId, @RequestBody TeamRosterDTO dto) {
        dto.setTeamId(teamId);
        TeamRosterDTO newRosterEntry = teamRosterService.addPlayerToTeam(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRosterEntry);
    }

    @Operation(summary = "Eliminar un jugador del roster del equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Jugador eliminado del roster"),
            @ApiResponse(responseCode = "404", description = "El jugador no se encontró en el roster de este equipo")
    })
    @DeleteMapping("/player/{playerId}")
    public ResponseEntity<Void> removePlayerFromTeam(@PathVariable Long teamId, @PathVariable Long playerId) {
        teamRosterService.removePlayerFromTeam(teamId, playerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Asignar un jugador como capitán del equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capitán asignado exitosamente"),
            @ApiResponse(responseCode = "404", description = "El jugador no se encontró en el roster de este equipo")
    })
    @PostMapping("/captain/{playerId}")
    public ResponseEntity<TeamRosterDTO> setCaptain(@PathVariable Long teamId, @PathVariable Long playerId) {
        return ResponseEntity.ok(teamRosterService.setCaptain(teamId, playerId));
    }
}
