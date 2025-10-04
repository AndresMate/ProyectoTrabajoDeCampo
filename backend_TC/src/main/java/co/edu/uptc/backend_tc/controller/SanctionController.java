package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.service.SanctionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanctions")
public class SanctionController {

    private final SanctionService sanctionService;

    public SanctionController(SanctionService sanctionService) {
        this.sanctionService = sanctionService;
    }

    @GetMapping("/team/{teamId}")
    public List<SanctionDTO> getSanctionsByTeam(@PathVariable Long teamId) {
        return sanctionService.getSanctionsByTeam(teamId);
    }

    @GetMapping("/player/{playerId}")
    public List<SanctionDTO> getSanctionsByPlayer(@PathVariable Long playerId) {
        return sanctionService.getSanctionsByPlayer(playerId);
    }

    @GetMapping("/match/{matchId}")
    public List<SanctionDTO> getSanctionsByMatch(@PathVariable Long matchId) {
        return sanctionService.getSanctionsByMatch(matchId);
    }

    @PostMapping
    public SanctionDTO addSanction(@RequestBody SanctionDTO dto) {
        return sanctionService.addSanction(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSanction(@PathVariable Long id) {
        sanctionService.deleteSanction(id);
    }
}
