package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionPlayerDTO;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.mapper.InscriptionPlayerMapper;
import co.edu.uptc.backend_tc.service.InscriptionPlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscription-players")
public class InscriptionPlayerController {

    private final InscriptionPlayerService service;

    public InscriptionPlayerController(InscriptionPlayerService service) {
        this.service = service;
    }

    @GetMapping
    public List<InscriptionPlayerDTO> getAll() {
        return service.getAll()
                .stream()
                .map(InscriptionPlayerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public InscriptionPlayerDTO create(@RequestBody InscriptionPlayerDTO dto) {
        Inscription inscription = service.getInscriptionById(dto.getInscriptionId());
        Player player = service.getPlayerById(dto.getPlayerId());
        InscriptionPlayer ip = InscriptionPlayerMapper.toEntity(dto, inscription, player);
        return InscriptionPlayerMapper.toDTO(service.create(ip));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
