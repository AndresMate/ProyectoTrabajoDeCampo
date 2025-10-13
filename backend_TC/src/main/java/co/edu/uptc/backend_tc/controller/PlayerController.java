package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.dto.filter.PlayerFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerResponseDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    public PageResponseDTO<PlayerDTO> getAll(Pageable pageable) {
        return playerService.getAll(pageable);
    }

    @PostMapping("/search")
    public PageResponseDTO<PlayerDTO> search(@RequestBody PlayerFilterDTO filter, Pageable pageable) {
        return playerService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public PlayerResponseDTO getById(@PathVariable Long id) {
        return playerService.getById(id);
    }

    @PostMapping
    public PlayerDTO create(@RequestBody PlayerDTO dto) {
        return playerService.create(dto);
    }

    @PutMapping("/{id}")
    public PlayerDTO update(@PathVariable Long id, @RequestBody PlayerDTO dto) {
        return playerService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.delete(id);
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
