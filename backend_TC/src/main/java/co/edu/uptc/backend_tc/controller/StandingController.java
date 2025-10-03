package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.StandingDTO;
import co.edu.uptc.backend_tc.entity.MatchResult;
import co.edu.uptc.backend_tc.repository.MatchResultRepository;
import co.edu.uptc.backend_tc.service.StandingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/standings")
public class StandingController {

    private final StandingService standingService;
    private final MatchResultRepository matchResultRepository;

    public StandingController(StandingService standingService, MatchResultRepository matchResultRepository) {
        this.standingService = standingService;
        this.matchResultRepository = matchResultRepository;
    }

    @GetMapping("/{tournamentId}/{categoryId}")
    public List<StandingDTO> getStandings(@PathVariable Long tournamentId, @PathVariable Long categoryId) {
        return standingService.getStandings(tournamentId, categoryId);
    }

    // 🔥 Nuevo endpoint: Recalcular standings completos
    @PostMapping("/{tournamentId}/{categoryId}/recalculate")
    public String recalculateStandings(@PathVariable Long tournamentId, @PathVariable Long categoryId) {
        List<MatchResult> results = matchResultRepository.findAll();
        standingService.recalculateStandings(
                tournamentId,
                categoryId,
                results.stream()
                        .filter(r -> r.getMatch().getTournament().getId().equals(tournamentId)
                                && r.getMatch().getCategory().getId().equals(categoryId))
                        .toList()
        );
        return "Standings recalculated for tournament " + tournamentId + " and category " + categoryId;
    }
}
