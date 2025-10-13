// src/main/java/co/edu/uptc/backend_tc/controller/ScenarioController.java
package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ScenarioDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    @GetMapping
    public ResponseEntity<?> getAll(Pageable pageable) {
        try {
            PageResponseDTO<ScenarioDTO> result = scenarioService.getAll(pageable);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al obtener escenarios: " + ex.getMessage());
        }
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<?> getByVenue(@PathVariable Long venueId) {
        try {
            List<ScenarioDTO> result = scenarioService.getByVenue(venueId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al obtener escenarios: " + ex.getMessage());
        }
    }

    @GetMapping("/night")
    public ResponseEntity<?> getScenariosForNightGames() {
        try {
            List<ScenarioDTO> result = scenarioService.getScenariosForNightGames();
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error al obtener escenarios nocturnos: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            ScenarioDTO result = scenarioService.getById(id);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ScenarioDTO dto) {
        try {
            ScenarioDTO result = scenarioService.create(dto);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ScenarioDTO dto) {
        try {
            ScenarioDTO result = scenarioService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            scenarioService.delete(id);
            return ResponseEntity.ok("Escenario eliminado correctamente");
        } catch (ResourceNotFoundException | BusinessException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
