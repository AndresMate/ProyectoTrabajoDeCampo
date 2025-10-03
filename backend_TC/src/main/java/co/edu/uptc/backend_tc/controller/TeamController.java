package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.entity.*;
import co.edu.uptc.backend_tc.mapper.TeamMapper;
import co.edu.uptc.backend_tc.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeamDTO> getAll() {
        return service.getAll()
                .stream()
                .map(TeamMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TeamDTO getById(@PathVariable Long id) {
        return TeamMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public TeamDTO create(@RequestBody TeamDTO dto) {
        Tournament tournament = service.getTournamentById(dto.getTournamentId());
        Category category = service.getCategoryById(dto.getCategoryId());
        Inscription inscription = dto.getOriginInscriptionId() != null
                ? service.getInscriptionById(dto.getOriginInscriptionId())
                : null;
        Club club = dto.getClubId() != null ? service.getClubById(dto.getClubId()) : null;

        Team team = TeamMapper.toEntity(dto, tournament, category, inscription, club);
        return TeamMapper.toDTO(service.create(team));
    }

    @PutMapping("/{id}")
    public TeamDTO update(@PathVariable Long id, @RequestBody TeamDTO dto) {
        Tournament tournament = service.getTournamentById(dto.getTournamentId());
        Category category = service.getCategoryById(dto.getCategoryId());
        Inscription inscription = dto.getOriginInscriptionId() != null
                ? service.getInscriptionById(dto.getOriginInscriptionId())
                : null;
        Club club = dto.getClubId() != null ? service.getClubById(dto.getClubId()) : null;

        Team team = TeamMapper.toEntity(dto, tournament, category, inscription, club);
        return TeamMapper.toDTO(service.update(id, team));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
