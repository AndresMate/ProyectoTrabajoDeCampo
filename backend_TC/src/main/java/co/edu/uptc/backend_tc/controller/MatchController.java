package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.service.MatchService;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Partidos", description = "Operaciones sobre los partidos del torneo")
public class MatchController {

    private final MatchService matchService;

    // --- Endpoints públicos ---
    @Operation(summary = "Obtener todos los partidos (Público)",
            description = "Devuelve todos los partidos con información completa (equipos, torneo, categoría, etc.)")
    @GetMapping("/public")
    public ResponseEntity<List<MatchResponseDTO>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @Operation(summary = "Obtener un partido por ID (Público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partido encontrado"),
            @ApiResponse(responseCode = "404", description = "Partido no encontrado")
    })
    @GetMapping("/public/{id}")
    public ResponseEntity<MatchResponseDTO> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    // --- Endpoints protegidos ---
    @Operation(summary = "Crear un nuevo partido",
            description = "Crea un nuevo partido. Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partido creado exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el torneo, categoría o equipos especificados")
    })
    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@RequestBody MatchDTO dto) {
        MatchDTO createdMatch = matchService.createMatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }

    @Operation(summary = "Eliminar un partido",
            description = "Elimina un partido. Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Partido eliminado exitosamente")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}