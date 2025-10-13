package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<ClubDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(clubService.getAll(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ClubDTO>> getAllActive() {
        return ResponseEntity.ok(clubService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClubDTO> create(@RequestBody ClubDTO dto) {
        return ResponseEntity.ok(clubService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubDTO> update(@PathVariable Long id, @RequestBody ClubDTO dto) {
        return ResponseEntity.ok(clubService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
