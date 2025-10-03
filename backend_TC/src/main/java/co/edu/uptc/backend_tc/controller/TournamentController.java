package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.mapper.TournamentMapper;
import co.edu.uptc.backend_tc.service.TournamentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    @GetMapping
    public List<TournamentDTO> getAll() {
        return service.getAll()
                .stream()
                .map(TournamentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TournamentDTO getById(@PathVariable Long id) {
        return TournamentMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public TournamentDTO create(@RequestBody TournamentDTO dto) {
        Sport sport = service.getSportById(dto.getSportId());
        User createdBy = service.getUserById(dto.getCreatedById());
        Tournament tournament = TournamentMapper.toEntity(dto, sport, createdBy);
        return TournamentMapper.toDTO(service.create(tournament));
    }

    @PutMapping("/{id}")
    public TournamentDTO update(@PathVariable Long id, @RequestBody TournamentDTO dto) {
        Sport sport = service.getSportById(dto.getSportId());
        User createdBy = service.getUserById(dto.getCreatedById());
        Tournament tournament = TournamentMapper.toEntity(dto, sport, createdBy);
        return TournamentMapper.toDTO(service.update(id, tournament));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
