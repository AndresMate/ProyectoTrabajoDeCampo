package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.mapper.VenueMapper;
import co.edu.uptc.backend_tc.service.VenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService service;

    public VenueController(VenueService service) {
        this.service = service;
    }

    @GetMapping
    public List<VenueDTO> getAll() {
        return service.getAll()
                .stream()
                .map(VenueMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VenueDTO getById(@PathVariable Long id) {
        return VenueMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public VenueDTO create(@RequestBody VenueDTO dto) {
        Venue venue = VenueMapper.toEntity(dto);
        return VenueMapper.toDTO(service.create(venue));
    }

    @PutMapping("/{id}")
    public VenueDTO update(@PathVariable Long id, @RequestBody VenueDTO dto) {
        Venue venue = VenueMapper.toEntity(dto);
        return VenueMapper.toDTO(service.update(id, venue));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
