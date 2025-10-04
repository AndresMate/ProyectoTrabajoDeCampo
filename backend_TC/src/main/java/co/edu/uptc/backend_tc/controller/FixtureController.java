package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.service.FixtureService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
public class FixtureController {

    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    @PostMapping("/round-robin/{tournamentId}/{categoryId}")
    public List<Match> generateRoundRobin(
            @PathVariable Long tournamentId,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "2025-01-01T10:00:00") String startDate
    ) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        return fixtureService.generateRoundRobin(tournamentId, categoryId, start);
    }
}
