package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.entity.Club;
import co.edu.uptc.backend_tc.mapper.ClubMapper;
import co.edu.uptc.backend_tc.service.ClubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService service;

    public ClubController(ClubService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClubDTO> getAll() {
        return service.getAll()
                .stream()
                .map(ClubMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ClubDTO getById(@PathVariable Long id) {
        return ClubMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public ClubDTO create(@RequestBody ClubDTO dto) {
        Club club = ClubMapper.toEntity(dto);
        return ClubMapper.toDTO(service.create(club));
    }

    @PutMapping("/{id}")
    public ClubDTO update(@PathVariable Long id, @RequestBody ClubDTO dto) {
        Club club = ClubMapper.toEntity(dto);
        return ClubMapper.toDTO(service.update(id, club));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
