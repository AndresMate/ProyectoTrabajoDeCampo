package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.service.TeamAvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/availability")
public class TeamAvailabilityController {

    private final TeamAvailabilityService service;

    public TeamAvailabilityController(TeamAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeamAvailabilityDTO> getAvailability(@PathVariable Long teamId) {
        return service.getByTeam(teamId);
    }

    @PostMapping
    public List<TeamAvailabilityDTO> saveAvailability(
            @PathVariable Long teamId,
            @RequestBody List<TeamAvailabilityDTO> dtoList,
            @RequestParam(defaultValue = "false") boolean nocturno
    ) {
        return service.saveAvailabilities(teamId, dtoList, nocturno);
    }
}
