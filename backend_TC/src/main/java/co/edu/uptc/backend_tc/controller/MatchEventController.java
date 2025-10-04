package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.service.MatchEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches/{matchId}/events")
public class MatchEventController {

    private final MatchEventService matchEventService;

    public MatchEventController(MatchEventService matchEventService) {
        this.matchEventService = matchEventService;
    }

    @GetMapping
    public List<MatchEventDTO> getEvents(@PathVariable Long matchId) {
        return matchEventService.getEventsByMatch(matchId);
    }

    @PostMapping
    public MatchEventDTO addEvent(@PathVariable Long matchId, @RequestBody MatchEventDTO dto) {
        dto.setMatchId(matchId);
        return matchEventService.addEvent(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        matchEventService.deleteEvent(id);
    }
}
