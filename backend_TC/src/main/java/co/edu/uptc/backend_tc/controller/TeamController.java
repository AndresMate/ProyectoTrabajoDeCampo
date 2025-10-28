package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.service.TeamService;
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
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Equipos", description = "Operaciones sobre los equipos del torneo")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    // 🔹 Cambiado: ahora devuelve TeamResponseDTO, no TeamDTO
    @Operation(summary = "Obtener todos los equipos paginados con información completa")
    @GetMapping
    public ResponseEntity<PageResponseDTO<TeamResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(teamService.getAll(pageable));
    }

    // 🔹 Nuevo endpoint opcional si quieres obtener todos sin paginación
    @Operation(summary = "Obtener todos los equipos sin paginación")
    @GetMapping("/all")
    public ResponseEntity<List<TeamResponseDTO>> getAllList() {
        return ResponseEntity.ok(teamService.getAllList());
    }

    @Operation(summary = "Obtener equipos por ID de torneo")
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<TeamDTO>> getByTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(teamService.getByTournament(tournamentId));
    }

    @Operation(summary = "Obtener equipos por torneo y categoría")
    @GetMapping("/tournament/{tournamentId}/category/{categoryId}")
    public ResponseEntity<List<TeamDTO>> getByTournamentAndCategory(
            @PathVariable Long tournamentId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(teamService.getByTournamentAndCategory(tournamentId, categoryId));
    }

    @Operation(summary = "Obtener un equipo por ID con estadísticas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo encontrado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getById(id));
    }

    @Operation(summary = "Crear un nuevo equipo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo creado exitosamente"),
            @ApiResponse(responseCode = "409", description = "Ya existe un equipo con ese nombre en el torneo"),
            @ApiResponse(responseCode = "404", description = "Torneo, categoría o club no encontrado")
    })
    @PostMapping
    public ResponseEntity<TeamDTO> create(@RequestBody TeamDTO dto) {
        TeamDTO createdTeam = teamService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
    }

    @Operation(summary = "Actualizar un equipo existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipo actualizado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflicto de nombre")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> update(@PathVariable Long id, @RequestBody TeamDTO dto) {
        return ResponseEntity.ok(teamService.update(id, dto));
    }

    @Operation(summary = "Desactivar un equipo (Soft Delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipo desactivado"),
            @ApiResponse(responseCode = "404", description = "Equipo no encontrado"),
            @ApiResponse(responseCode = "400", description = "El equipo tiene partidos jugados y no puede ser eliminado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

}