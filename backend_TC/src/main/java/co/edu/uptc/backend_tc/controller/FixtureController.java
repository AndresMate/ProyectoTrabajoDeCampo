// src/main/java/co/edu/uptc/backend_tc/controller/FixtureController.java
package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.service.FixtureService;
import co.edu.uptc.backend_tc.repository.MatchRepository;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
@Tag(name = "Fixtures", description = "Gestión y generación de fixtures de torneos")
public class FixtureController {

    private final FixtureService fixtureService;
    private final MatchRepository matchRepository;

    public FixtureController(FixtureService fixtureService, MatchRepository matchRepository) {
        this.fixtureService = fixtureService;
        this.matchRepository = matchRepository;
    }

    @Operation(
            summary = "Generar fixture automáticamente",
            description = """
                    Genera automáticamente los partidos de un torneo según el modo elegido.
                    - `round_robin`: todos contra todos.
                    - `knockout`: eliminación directa.
                    Solo se incluyen equipos con inscripción aprobada.
                    Los horarios se asignan únicamente si ambos equipos tienen disponibilidad compatible.
                    """
    )
    @PostMapping("/generate")
    public ResponseEntity<?> generateFixture(
            @Parameter(description = "ID del torneo", example = "1")
            @RequestParam Long tournamentId,
            @Parameter(description = "ID de la categoría", example = "2")
            @RequestParam Long categoryId,
            @Parameter(description = "Modo del fixture: round_robin o knockout", example = "round_robin")
            @RequestParam String mode
    ) {
        try {
            fixtureService.generateFixture(tournamentId, categoryId, mode);
            return ResponseEntity.ok("Fixture generado exitosamente en modo " + mode);
        } catch (ResourceNotFoundException | BadRequestException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Regenerar fixture",
            description = "Elimina los partidos anteriores del torneo y genera uno nuevo según el modo indicado."
    )
    @PostMapping("/regenerate")
    public ResponseEntity<?> regenerateFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId,
            @RequestParam String mode
    ) {
        try {
            fixtureService.deleteFixture(tournamentId, categoryId);
            fixtureService.generateFixture(tournamentId, categoryId, mode);
            return ResponseEntity.ok("Fixture regenerado correctamente en modo " + mode);
        } catch (ResourceNotFoundException | BadRequestException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar fixture de un torneo y categoría")
    @DeleteMapping
    public ResponseEntity<?> deleteFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        try {
            fixtureService.deleteFixture(tournamentId, categoryId);
            return ResponseEntity.ok("Fixture eliminado correctamente");
        } catch (ResourceNotFoundException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @Operation(summary = "Listar todos los partidos generados para un torneo y categoría")
    @GetMapping("/list")
    public ResponseEntity<?> listFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        try {
            List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
            if (matches.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(matches);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al listar los partidos: " + ex.getMessage());
        }
    }
}
