package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ForbiddenException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<InscriptionResponseDTO> result = inscriptionService.getAll();
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al obtener inscripciones: " + ex.getMessage());
        }
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> getByTournament(@PathVariable Long tournamentId) {
        try {
            List<InscriptionResponseDTO> result = inscriptionService.getByTournament(tournamentId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al obtener inscripciones: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            InscriptionResponseDTO result = inscriptionService.getById(id);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InscriptionDTO dto) {
        try {
            InscriptionResponseDTO result = inscriptionService.create(dto);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException | BusinessException | ConflictException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody User approver) {
        try {
            InscriptionResponseDTO result = inscriptionService.approve(id, approver);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException | BusinessException | ForbiddenException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody User rejector) {
        try {
            InscriptionResponseDTO result = inscriptionService.reject(id, rejector);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException | BusinessException | ForbiddenException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestBody User deleter) {
        try {
            inscriptionService.delete(id, deleter);
            return ResponseEntity.ok("Inscripción eliminada correctamente");
        } catch (ResourceNotFoundException | BusinessException | ForbiddenException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
