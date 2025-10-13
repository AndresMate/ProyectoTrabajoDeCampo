package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionPlayerDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.InscriptionPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscription-players")
@RequiredArgsConstructor
public class InscriptionPlayerController {

    private final InscriptionPlayerService service;

    @GetMapping("/{inscriptionId}")
    public ResponseEntity<List<InscriptionPlayerDTO>> getByInscription(@PathVariable Long inscriptionId) {
        return ResponseEntity.ok(service.getByInscription(inscriptionId));
    }

    @PostMapping
    public ResponseEntity<InscriptionPlayerDTO> addPlayerToInscription(@RequestBody InscriptionPlayerDTO dto) {
        try {
            return ResponseEntity.ok(service.addPlayerToInscription(dto));
        } catch (ResourceNotFoundException | BusinessException | ConflictException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removePlayerFromInscription(
            @RequestParam Long inscriptionId,
            @RequestParam Long playerId) {
        try {
            service.removePlayerFromInscription(inscriptionId, playerId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException | BusinessException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
