package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.dto.response.MatchEventResponseDTO;
import co.edu.uptc.backend_tc.service.MatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-events")
@RequiredArgsConstructor
public class MatchEventController {

    private final MatchEventService matchEventService;

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MatchEventResponseDTO>> getByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchEventService.getByMatch(matchId));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchEventDTO>> getByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(matchEventService.getByPlayer(playerId));
    }

    @PostMapping
    public ResponseEntity<MatchEventDTO> create(@RequestBody MatchEventDTO dto) {
        return ResponseEntity.ok(matchEventService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
