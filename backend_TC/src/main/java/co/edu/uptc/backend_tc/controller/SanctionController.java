package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.dto.response.SanctionResponseDTO;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.SanctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final SanctionService sanctionService;

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<SanctionResponseDTO>> getSanctionsByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByTeam(teamId));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<SanctionResponseDTO>> getSanctionsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByPlayer(playerId));
    }

    @GetMapping("/player/{playerId}/active")
    public ResponseEntity<List<SanctionResponseDTO>> getActiveSanctionsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(sanctionService.getActiveSanctionsByPlayer(playerId));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<SanctionResponseDTO>> getSanctionsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(sanctionService.getSanctionsByMatch(matchId));
    }

    @PostMapping
    public ResponseEntity<SanctionDTO> addSanction(@RequestBody SanctionDTO dto) {
        SanctionDTO created = sanctionService.addSanction(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanction(@PathVariable Long id) {
        sanctionService.deleteSanction(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
