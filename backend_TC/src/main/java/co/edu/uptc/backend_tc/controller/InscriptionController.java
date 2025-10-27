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

// ==========================
// VALIDACIONES PREVIAS
// ==========================

    @Operation(summary = "Verificar disponibilidad de nombre de equipo")
    @GetMapping("/check-team-name")
    public ResponseEntity<Map<String, Boolean>> checkTeamName(
            @RequestParam Long tournamentId,
            @RequestParam String teamName) {
        boolean isAvailable = inscriptionService.isTeamNameAvailable(tournamentId, teamName);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    @Operation(summary = "Verificar si un club ya está inscrito en el torneo")
    @GetMapping("/check-club")
    public ResponseEntity<Map<String, Boolean>> checkClub(
            @RequestParam Long tournamentId,
            @RequestParam Long clubId) {
        boolean isAvailable = !inscriptionService.isClubRegistered(tournamentId, clubId);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    @Operation(summary = "Verificar si un jugador ya está inscrito en otro equipo del torneo")
    @GetMapping("/check-player")
    public ResponseEntity<Map<String, Boolean>> checkPlayer(
            @RequestParam Long tournamentId,
            @RequestParam String documentNumber) {
        boolean isAvailable = inscriptionService.isPlayerAvailable(tournamentId, documentNumber);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    // ==========================
    // CREACIÓN DE INSCRIPCIONES
    // ==========================

    @Operation(summary = "Crear inscripción completa con jugadores y disponibilidad")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Nombre de equipo, club o jugadores duplicados")
    })
    @PostMapping
    public ResponseEntity<InscriptionResponseDTO> create(@RequestBody InscriptionDTO dto) {
        InscriptionResponseDTO createdInscription = inscriptionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInscription);
    }

    // ==========================
    // CONSULTAS
    // ==========================

    @Operation(summary = "Obtener una inscripción por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<InscriptionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.getById(id));
    }

    @Operation(summary = "Obtener inscripciones aprobadas por torneo")
    @GetMapping("/tournament/{tournamentId}/approved")
    public ResponseEntity<List<InscriptionResponseDTO>> getApprovedByTournament(
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(inscriptionService.getApprovedByTournament(tournamentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================
    // ADMINISTRACIÓN
    // ==========================

    @Operation(summary = "Obtener todas las inscripciones (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin")
    public ResponseEntity<List<InscriptionResponseDTO>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    @Operation(summary = "Aprobar inscripción (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/{id}/approve")
    public ResponseEntity<InscriptionResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.approve(id));
    }

    @Operation(summary = "Rechazar inscripción (Admin)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/{id}/reject")
    public ResponseEntity<InscriptionResponseDTO> reject(
            @PathVariable Long id,
            @RequestBody InscriptionStatusUpdateDTO updateDTO) {
        return ResponseEntity.ok(inscriptionService.reject(id, updateDTO.getReason()));
    }
}
