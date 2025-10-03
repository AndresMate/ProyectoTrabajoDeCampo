package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.mapper.SportMapper;
import co.edu.uptc.backend_tc.service.SportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sports")
public class SportController {

    private final SportService service;

    public SportController(SportService service) {
        this.service = service;
    }

    @GetMapping
    public List<SportDTO> getAll() {
        return service.getAll()
                .stream()
                .map(SportMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SportDTO getById(@PathVariable Long id) {
        return SportMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public SportDTO create(@RequestBody SportDTO dto) {
        Sport sport = SportMapper.toEntity(dto);
        return SportMapper.toDTO(service.create(sport));
    }

    @PutMapping("/{id}")
    public SportDTO update(@PathVariable Long id, @RequestBody SportDTO dto) {
        Sport sport = SportMapper.toEntity(dto);
        return SportMapper.toDTO(service.update(id, sport));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
