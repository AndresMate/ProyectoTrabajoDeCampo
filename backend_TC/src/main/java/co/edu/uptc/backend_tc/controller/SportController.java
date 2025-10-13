package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @GetMapping
    public PageResponseDTO<SportDTO> getAll(Pageable pageable) {
        return sportService.getAll(pageable);
    }

    @GetMapping("/active")
    public List<SportDTO> getAllActive() {
        return sportService.getAllActive();
    }

    @GetMapping("/{id}")
    public SportDTO getById(@PathVariable Long id) {
        return sportService.getById(id);
    }

    @PostMapping
    public SportDTO create(@RequestBody SportDTO dto) {
        return sportService.create(dto);
    }

    @PutMapping("/{id}")
    public SportDTO update(@PathVariable Long id, @RequestBody SportDTO dto) {
        return sportService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        sportService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepciones
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
