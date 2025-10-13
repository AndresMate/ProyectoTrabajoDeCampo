package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TournamentDTO;
import co.edu.uptc.backend_tc.dto.filter.TournamentFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TournamentResponseDTO;
import co.edu.uptc.backend_tc.exception.BadRequestException;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public PageResponseDTO<TournamentResponseDTO> getAll(Pageable pageable) {
        return tournamentService.getAll(pageable);
    }

    @PostMapping("/search")
    public PageResponseDTO<TournamentResponseDTO> search(
            @RequestBody TournamentFilterDTO filter,
            Pageable pageable) {
        return tournamentService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public TournamentResponseDTO getById(@PathVariable Long id) {
        return tournamentService.getById(id);
    }

    @PostMapping
    public TournamentResponseDTO create(@RequestBody TournamentDTO dto) {
        return tournamentService.create(dto);
    }

    @PutMapping("/{id}")
    public TournamentResponseDTO update(@PathVariable Long id, @RequestBody TournamentDTO dto) {
        return tournamentService.update(id, dto);
    }

    @PostMapping("/{id}/start")
    public TournamentResponseDTO startTournament(@PathVariable Long id) {
        return tournamentService.startTournament(id);
    }

    @PostMapping("/{id}/complete")
    public TournamentResponseDTO completeTournament(@PathVariable Long id) {
        return tournamentService.completeTournament(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepciones
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
