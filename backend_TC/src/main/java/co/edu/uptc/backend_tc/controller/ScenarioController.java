package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.entity.Scenario;
import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.mapper.ScenarioMapper;
import co.edu.uptc.backend_tc.service.ScenarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioService service;

    public ScenarioController(ScenarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<ScenarioDTO> getAll() {
        return service.getAll()
                .stream()
                .map(ScenarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ScenarioDTO getById(@PathVariable Long id) {
        return ScenarioMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public ScenarioDTO create(@RequestBody ScenarioDTO dto) {
        Venue venue = service.getVenueById(dto.getVenueId());
        Scenario scenario = ScenarioMapper.toEntity(dto, venue);
        return ScenarioMapper.toDTO(service.create(scenario));
    }

    @PutMapping("/{id}")
    public ScenarioDTO update(@PathVariable Long id, @RequestBody ScenarioDTO dto) {
        Venue venue = service.getVenueById(dto.getVenueId());
        Scenario scenario = ScenarioMapper.toEntity(dto, venue);
        return ScenarioMapper.toDTO(service.update(id, scenario));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
