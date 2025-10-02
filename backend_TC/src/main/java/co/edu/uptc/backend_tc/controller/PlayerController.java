package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.mapper.PlayerMapper;
import co.edu.uptc.backend_tc.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlayerDTO> getAll() {
        return service.getAll()
                .stream()
                .map(PlayerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PlayerDTO getById(@PathVariable Long id) {
        return PlayerMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public PlayerDTO create(@RequestBody PlayerDTO dto) {
        Player player = PlayerMapper.toEntity(dto);
        return PlayerMapper.toDTO(service.create(player));
    }

    @PutMapping("/{id}")
    public PlayerDTO update(@PathVariable Long id, @RequestBody PlayerDTO dto) {
        Player player = PlayerMapper.toEntity(dto);
        return PlayerMapper.toDTO(service.update(id, player));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
