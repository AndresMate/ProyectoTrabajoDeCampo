package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.InscriptionStatusUpdateDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.service.InscriptionService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Operaciones sobre las inscripciones de equipos a torneos")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    // ✅ ENDPOINT NUEVO: Crear inscripción completa con jugadores y fotos
    @Operation(summary = "Crear inscripción completa con jugadores",
            description = "Endpoint público para inscribir equipo con todos sus jugadores e imágenes de carnets")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Nombre de equipo o datos de jugadores duplicados")
    })
    @PostMapping
    public ResponseEntity<InscriptionResponseDTO> create(@RequestBody InscriptionDTO dto) {
        InscriptionResponseDTO createdInscription = inscriptionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInscription);
    }

    @Operation(summary = "Obtener inscripciones aprobadas por torneo")
    @GetMapping("/tournament/{tournamentId}/approved")
    public ResponseEntity<List<InscriptionResponseDTO>> getApprovedByTournament(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(inscriptionService.getApprovedByTournament(tournamentId));
    }

    @Operation(summary = "Obtener una inscripción por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<InscriptionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.getById(id));
    }

    @GetMapping("/check-team-name")
    public ResponseEntity<Map<String, Boolean>> isTeamNameAvailable(
            @RequestParam Long tournamentId,
            @RequestParam String teamName) {
        boolean isAvailable = inscriptionService.isTeamNameAvailable(tournamentId, teamName);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === ENDPOINTS DE ADMINISTRACIÓN ===

    @Operation(summary = "Obtener todas las inscripciones (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin")
    public ResponseEntity<List<InscriptionResponseDTO>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    @Operation(summary = "Aprobar una inscripción (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<InscriptionResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.approve(id));
    }

    @Operation(summary = "Rechazar una inscripción (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<InscriptionResponseDTO> reject(
            @PathVariable Long id,
            @RequestBody InscriptionStatusUpdateDTO updateDTO) {
        return ResponseEntity.ok(inscriptionService.reject(id, updateDTO.getReason()));
    }
}