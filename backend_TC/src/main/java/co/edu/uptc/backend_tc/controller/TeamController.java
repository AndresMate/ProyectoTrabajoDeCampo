// Java
package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Operaciones sobre equipos")
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Listar equipos paginados")
    @GetMapping
    public PageResponseDTO<TeamDTO> getAll(Pageable pageable) {
        return teamService.getAll(pageable);
    }

    @Operation(summary = "Listar equipos por torneo")
    @GetMapping("/by-tournament/{tournamentId}")
    public List<TeamDTO> getByTournament(@PathVariable Long tournamentId) {
        return teamService.getByTournament(tournamentId);
    }

    @Operation(summary = "Listar equipos por torneo y categoría")
    @GetMapping("/by-tournament-category")
    public List<TeamDTO> getByTournamentAndCategory(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        return teamService.getByTournamentAndCategory(tournamentId, categoryId);
    }

    @Operation(summary = "Obtener equipo por id")
    @GetMapping("/{id}")
    public TeamResponseDTO getById(@PathVariable Long id) {
        return teamService.getById(id);
    }

    @Operation(summary = "Crear equipo")
    @PostMapping
    public TeamDTO create(@RequestBody TeamDTO dto) {
        return teamService.create(dto);
    }

    @Operation(summary = "Actualizar equipo")
    @PutMapping("/{id}")
    public TeamDTO update(@PathVariable Long id, @RequestBody TeamDTO dto) {
        return teamService.update(id, dto);
    }

    @Operation(summary = "Eliminar equipo")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }
}
