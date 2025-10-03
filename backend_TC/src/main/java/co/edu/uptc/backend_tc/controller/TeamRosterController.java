package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamRoster;
import co.edu.uptc.backend_tc.mapper.TeamRosterMapper;
import co.edu.uptc.backend_tc.service.TeamRosterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team-roster")
public class TeamRosterController {

    private final TeamRosterService service;

    public TeamRosterController(TeamRosterService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeamRosterDTO> getAll() {
        return service.getAll()
                .stream()
                .map(TeamRosterMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public TeamRosterDTO create(@RequestBody TeamRosterDTO dto) {
        Team team = service.getTeamById(dto.getTeamId());
        Player player = service.getPlayerById(dto.getPlayerId());
        TeamRoster tr = TeamRosterMapper.toEntity(dto, team, player);
        return TeamRosterMapper.toDTO(service.create(tr));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
