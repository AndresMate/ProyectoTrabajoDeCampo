package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.service.MatchEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-events")
@Tag(name = "Match Events", description = "Manage match events like goals, assists, and cards")
public class MatchEventController {

    private final MatchEventService matchEventService;

    public MatchEventController(MatchEventService matchEventService) {
        this.matchEventService = matchEventService;
    }

    @Operation(summary = "Register a new match event")
    @PostMapping
    public ResponseEntity<MatchEventDTO> create(@RequestBody MatchEventDTO dto) {
        return ResponseEntity.ok(matchEventService.create(dto));
    }

    @Operation(summary = "List events by match")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MatchEventDTO>> getByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchEventService.getByMatch(matchId));
    }

    @Operation(summary = "List events by player")
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<MatchEventDTO>> getByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(matchEventService.getByPlayer(playerId));
    }

    @Operation(summary = "Delete a match event")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
