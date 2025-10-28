package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fixtures")
@RequiredArgsConstructor
@Tag(name = "Fixture", description = "Operaciones para generar el calendario de partidos")
@SecurityRequirement(name = "bearerAuth")
public class FixtureController {

    private final FixtureService fixtureService;

    @Operation(
            summary = "Generar fixture para un torneo y categoría",
            description = "Crea los partidos según el modo seleccionado ('round_robin' o 'knockout'). Requiere rol ADMIN o SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fixture generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Modo de fixture inválido o no hay suficientes equipos")
    })
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId,
            @RequestParam String mode) {

        int matchesCreated = fixtureService.generateFixture(tournamentId, categoryId, mode);
        return ResponseEntity.ok(Map.of(
                "message", "Fixture generado exitosamente",
                "matchesCreated", matchesCreated
        ));
    }

    @Operation(
            summary = "Eliminar fixture de un torneo y categoría",
            description = "Elimina todos los partidos previamente generados. Requiere rol ADMIN o SUPER_ADMIN"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fixture eliminado exitosamente")
    })
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteFixture(
            @RequestParam Long tournamentId,
            @RequestParam Long categoryId) {
        fixtureService.deleteFixture(tournamentId, categoryId);
        return ResponseEntity.ok(Map.of("message", "Fixture eliminado exitosamente"));
    }
}