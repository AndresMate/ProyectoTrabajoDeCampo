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

    // Endpoint mejorado: Recalcular standings completos
    @PostMapping("/{tournamentId}/{categoryId}/recalculate")
    public String recalculateStandings(@PathVariable Long tournamentId, @PathVariable Long categoryId) {
        List<MatchResult> results = matchResultRepository.findByTournamentIdAndCategoryId(tournamentId, categoryId);
        standingService.recalculateStandings(tournamentId, categoryId, results);
        return "Standings recalculated for tournament " + tournamentId + " and category " + categoryId;
    }
}