package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.dto.stats.InscriptionStatsDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ForbiddenException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.InscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@Tag(name = "Inscripciones", description = "Gestión de inscripciones a torneos")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    // === 🔐 SOLO ADMIN Y SUPER_ADMIN ===

    @Operation(summary = "Obtener todas las inscripciones (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<InscriptionResponseDTO>> getAll() {
        List<InscriptionResponseDTO> result = inscriptionService.getAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener inscripciones por torneo (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<InscriptionResponseDTO>> getByTournament(@PathVariable Long tournamentId) {
        List<InscriptionResponseDTO> result = inscriptionService.getByTournament(tournamentId);
        return ResponseEntity.ok(result);
    }

    // === 🔓 PÚBLICO (usuarios normales sin autenticación) ===

    @Operation(summary = "Crear inscripción (PÚBLICO)")
    @PostMapping
    public ResponseEntity<InscriptionResponseDTO> create(@RequestBody InscriptionDTO dto) {
        InscriptionResponseDTO result = inscriptionService.create(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Consultar inscripción por ID (PÚBLICO)")
    @GetMapping("/{id}")
    public ResponseEntity<InscriptionResponseDTO> getById(@PathVariable Long id) {
        InscriptionResponseDTO result = inscriptionService.getById(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Consultar mis inscripciones (PÚBLICO - por email del delegado)")
    @GetMapping("/my-inscriptions/{delegateEmail}")
    public ResponseEntity<List<InscriptionResponseDTO>> getMyInscriptions(@PathVariable String delegateEmail) {
        List<InscriptionResponseDTO> result = inscriptionService.getByDelegateEmail(delegateEmail);
        return ResponseEntity.ok(result);
    }

    // === 🔐 SOLO ADMIN Y SUPER_ADMIN (aprobación/rechazo) ===

    @Operation(summary = "Aprobar inscripción (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<InscriptionResponseDTO> approve(@PathVariable Long id) {
        InscriptionResponseDTO result = inscriptionService.approve(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Rechazar inscripción (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<InscriptionResponseDTO> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        InscriptionResponseDTO result = inscriptionService.reject(id, reason);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Eliminar inscripción (ADMIN)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // === 📊 ENDPOINTS PÚBLICOS ADICIONALES ===

    @Operation(summary = "Obtener inscripciones aprobadas por torneo (PÚBLICO)")
    @GetMapping("/public/tournament/{tournamentId}/approved")
    public ResponseEntity<List<InscriptionResponseDTO>> getApprovedByTournament(@PathVariable Long tournamentId) {
        List<InscriptionResponseDTO> result = inscriptionService.getApprovedByTournament(tournamentId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Verificar disponibilidad de nombre de equipo (PÚBLICO)")
    @GetMapping("/public/check-team-name")
    public ResponseEntity<Boolean> checkTeamNameAvailability(
            @RequestParam Long tournamentId,
            @RequestParam String teamName) {
        boolean isAvailable = inscriptionService.isTeamNameAvailable(tournamentId, teamName);
        return ResponseEntity.ok(isAvailable);
    }

    // === 🏟️ ENDPOINTS PARA DASHBOARD ADMIN ===



}