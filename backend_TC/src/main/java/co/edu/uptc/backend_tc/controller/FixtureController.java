package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.service.FixtureService;
import co.edu.uptc.backend_tc.repository.MatchRepository;
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

    // 🔹 Generar fixture
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
    public ResponseEntity<String> generateFixture(
            @Parameter(description = "ID del torneo", example = "1")
            @RequestParam Long tournamentId,

            @Parameter(description = "ID de la categoría", example = "2")
            @RequestParam Long categoryId,

            @Parameter(description = "Modo del fixture: round_robin o knockout", example = "round_robin")
            @RequestParam String mode
    ) {
        fixtureService.generateFixture(tournamentId, categoryId, mode);
        return ResponseEntity.ok("Fixture generado exitosamente en modo " + mode);
    }

    // 🔹 Regenerar fixture
    @Operation(
            summary = "Regenerar fixture",
            description = "Elimina los partidos anteriores del torneo y genera uno nuevo según el modo indicado."
    )
    @PostMapping("/regenerate")
    public ResponseEntity<String> regenerateFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId,
            @RequestParam String mode
    ) {
        fixtureService.deleteFixture(tournamentId, categoryId);
        fixtureService.generateFixture(tournamentId, categoryId, mode);
        return ResponseEntity.ok("Fixture regenerado correctamente en modo " + mode);
    }

    // 🔹 Eliminar fixture
    @Operation(summary = "Eliminar fixture de un torneo y categoría")
    @DeleteMapping
    public ResponseEntity<String> deleteFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        fixtureService.deleteFixture(tournamentId, categoryId);
        return ResponseEntity.ok("Fixture eliminado correctamente");
    }

    // 🔹 Listar partidos generados
    @Operation(summary = "Listar todos los partidos generados para un torneo y categoría")
    @GetMapping("/list")
    public ResponseEntity<List<Match>> listFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId
    ) {
        List<Match> matches = matchRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        if (matches.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(matches);
    }
}
