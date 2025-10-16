package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.ScenarioService;
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
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
@Tag(name = "Escenarios", description = "Operaciones sobre los escenarios deportivos")
@SecurityRequirement(name = "bearerAuth")
public class ScenarioController {

    private final ScenarioService scenarioService;

    @Operation(summary = "Obtener todos los escenarios paginados")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ScenarioDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(scenarioService.getAll(pageable));
    }

    @Operation(summary = "Obtener escenarios por ID de sede")
    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<ScenarioDTO>> getByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(scenarioService.getByVenue(venueId));
    }

    @Operation(summary = "Obtener escenarios que soportan partidos nocturnos")
    @GetMapping("/night-games")
    public ResponseEntity<List<ScenarioDTO>> getForNightGames() {
        return ResponseEntity.ok(scenarioService.getScenariosForNightGames());
    }

    @Operation(summary = "Obtener un escenario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Escenario encontrado"),
        @ApiResponse(responseCode = "404", description = "Escenario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScenarioDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scenarioService.getById(id));
    }

    @Operation(summary = "Crear un nuevo escenario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Escenario creado"),
        @ApiResponse(responseCode = "404", description = "Sede no encontrada")
    })
    @PostMapping
    public ResponseEntity<ScenarioDTO> create(@RequestBody ScenarioDTO dto) {
        ScenarioDTO createdScenario = scenarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdScenario);
    }

    @Operation(summary = "Actualizar un escenario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Escenario actualizado"),
        @ApiResponse(responseCode = "404", description = "Escenario o sede no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ScenarioDTO> update(@PathVariable Long id, @RequestBody ScenarioDTO dto) {
        return ResponseEntity.ok(scenarioService.update(id, dto));
    }

    @Operation(summary = "Eliminar un escenario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Escenario eliminado"),
        @ApiResponse(responseCode = "404", description = "Escenario no encontrado"),
        @ApiResponse(responseCode = "400", description = "El escenario tiene partidos programados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scenarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}