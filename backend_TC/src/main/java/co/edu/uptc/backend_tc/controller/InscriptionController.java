package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.mapper.InscriptionMapper;
import co.edu.uptc.backend_tc.service.InscriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscriptions")
public class InscriptionController {

    private final InscriptionService service;

    public InscriptionController(InscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public List<InscriptionDTO> getAll() {
        return service.getAll()
                .stream()
                .map(InscriptionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public InscriptionDTO getById(@PathVariable Long id) {
        return InscriptionMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public InscriptionDTO create(@RequestBody InscriptionDTO dto) {
        Tournament tournament = service.getTournamentById(dto.getTournamentId());
        Category category = service.getCategoryById(dto.getCategoryId());
        Player delegate = service.getPlayerById(dto.getDelegatePlayerId());
        Inscription inscription = InscriptionMapper.toEntity(dto, tournament, category, delegate);
        return InscriptionMapper.toDTO(service.create(inscription));
    }

    @PutMapping("/{id}")
    public InscriptionDTO update(@PathVariable Long id, @RequestBody InscriptionDTO dto) {
        Tournament tournament = service.getTournamentById(dto.getTournamentId());
        Category category = service.getCategoryById(dto.getCategoryId());
        Player delegate = service.getPlayerById(dto.getDelegatePlayerId());
        Inscription inscription = InscriptionMapper.toEntity(dto, tournament, category, delegate);
        return InscriptionMapper.toDTO(service.update(id, inscription));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
