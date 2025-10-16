package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.service.MatchResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match-results")
@RequiredArgsConstructor
@Tag(name = "Resultados de Partido", description = "Operaciones sobre los resultados de los partidos")
@SecurityRequirement(name = "bearerAuth")
public class MatchResultController {

    private final MatchResultService matchResultService;

    @Operation(summary = "Obtener resultado por ID de partido", description = "Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado encontrado"),
        @ApiResponse(responseCode = "404", description = "No se ha registrado resultado para este partido")
    })
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResultDTO> getResultByMatchId(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchResultService.getResultByMatchId(matchId));
    }

    @Operation(summary = "Registrar un resultado de partido", description = "Crea el resultado y marca el partido como finalizado. Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El partido ya estaba finalizado"),
        @ApiResponse(responseCode = "404", description = "Partido no encontrado")
    })
    @PostMapping
    public ResponseEntity<MatchResultDTO> registerResult(@RequestBody MatchResultDTO dto) {
        return ResponseEntity.ok(matchResultService.registerOrUpdateResult(dto));
    }

    @Operation(summary = "Actualizar un resultado de partido", description = "Modifica un resultado ya existente y recalcula la tabla de posiciones. Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Resultado de partido no encontrado")
    })
    @PutMapping
    public ResponseEntity<MatchResultDTO> updateResult(@RequestBody MatchResultDTO dto) {
        return ResponseEntity.ok(matchResultService.updateResult(dto));
    }

    @Operation(summary = "Eliminar un resultado de partido", description = "Elimina el resultado y revierte el estado del partido a 'Programado'. Requiere rol REFEREE, ADMIN o SUPER_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Resultado eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Resultado de partido no encontrado")
    })
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long matchId) {
        matchResultService.deleteResult(matchId);
        return ResponseEntity.noContent().build();
    }
}