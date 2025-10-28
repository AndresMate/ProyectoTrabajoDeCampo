package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.dto.response.MatchResponseDTO;
import co.edu.uptc.backend_tc.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Partidos", description = "Gestión de partidos del torneo")
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "Obtener todos los partidos con información completa")
    @GetMapping
    public ResponseEntity<List<MatchResponseDTO>> getAll() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @Operation(summary = "Obtener partido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @Operation(summary = "Crear un nuevo partido")
    @PostMapping
    public ResponseEntity<MatchDTO> create(@RequestBody MatchDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(dto));
    }

    @Operation(summary = "Eliminar un partido")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}
