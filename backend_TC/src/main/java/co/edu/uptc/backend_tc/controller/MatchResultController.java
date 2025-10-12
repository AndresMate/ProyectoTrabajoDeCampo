package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.service.MatchResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-results")
@Tag(name = "Match Results", description = "Endpoints for managing match results and standings updates")
public class MatchResultController {

    private final MatchResultService matchResultService;

    public MatchResultController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @Operation(summary = "Register a new match result")
    @PostMapping
    public ResponseEntity<MatchResultDTO> createResult(@RequestBody MatchResultDTO dto) {
        return ResponseEntity.ok(matchResultService.registerOrUpdateResult(dto));
    }

    @Operation(summary = "Update an existing match result")
    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResultDTO> updateResult(@PathVariable Long matchId,
                                                       @RequestBody MatchResultDTO dto) {
        dto.setMatchId(matchId);
        return ResponseEntity.ok(matchResultService.updateResult(dto));
    }

    @Operation(summary = "Get result by match ID")
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResultDTO> getResult(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchResultService.getResultByMatchId(matchId));
    }

    @Operation(summary = "Delete match result")
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteResult(@PathVariable Long matchId) {
        matchResultService.deleteResult(matchId);
        return ResponseEntity.noContent().build();
    }
}
