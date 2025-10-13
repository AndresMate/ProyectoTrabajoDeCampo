package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.VenueDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.exception.BusinessException;
import co.edu.uptc.backend_tc.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
@Tag(name = "Venue", description = "Operaciones sobre venues")
public class VenueController {

    private final VenueService venueService;

    @Operation(summary = "Obtener todos los venues paginados")
    @GetMapping
    public PageResponseDTO<VenueDTO> getAll(Pageable pageable) {
        return venueService.getAll(pageable);
    }

    @Operation(summary = "Buscar venues por nombre")
    @GetMapping("/search")
    public List<VenueDTO> searchByName(@RequestParam String name) {
        return venueService.searchByName(name);
    }

    @Operation(summary = "Obtener venue por id")
    @GetMapping("/{id}")
    public VenueDTO getById(@PathVariable Long id) {
        return venueService.getById(id);
    }

    @Operation(summary = "Crear un nuevo venue")
    @PostMapping
    public VenueDTO create(@RequestBody VenueDTO dto) {
        return venueService.create(dto);
    }

    @Operation(summary = "Actualizar un venue")
    @PutMapping("/{id}")
    public VenueDTO update(@PathVariable Long id, @RequestBody VenueDTO dto) {
        return venueService.update(id, dto);
    }

    @Operation(summary = "Eliminar un venue")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }
}
