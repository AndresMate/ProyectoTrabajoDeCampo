package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.service.MatchResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches/{matchId}/result")
public class MatchResultController {

    private final MatchResultService matchResultService;

    public MatchResultController(MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @GetMapping
    public MatchResultDTO getResult(@PathVariable Long matchId) {
        return matchResultService.getResult(matchId);
    }

    @PostMapping
    public MatchResultDTO saveResult(@PathVariable Long matchId, @RequestBody MatchResultDTO dto) {
        dto.setMatchId(matchId);
        return matchResultService.saveResult(dto);
    }

    @DeleteMapping
    public void deleteResult(@PathVariable Long matchId) {
        matchResultService.deleteResult(matchId);
    }
}
