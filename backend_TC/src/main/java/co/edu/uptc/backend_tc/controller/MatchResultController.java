package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.service.MatchResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match-results")
public class MatchResultController {

    private final MatchResultService matchResultService;

    public MatchResultController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @GetMapping("/{matchId}")
    public MatchResultDTO getByMatchId(@PathVariable Long matchId) {
        return matchResultService.getByMatchId(matchId);
    }

    @PostMapping
    public MatchResultDTO createOrUpdateResult(@RequestBody MatchResultDTO dto) {
        return matchResultService.createOrUpdateResult(dto);
    }

    @DeleteMapping("/{matchId}")
    public void deleteResult(@PathVariable Long matchId) {
        matchResultService.deleteResult(matchId);
    }
}
