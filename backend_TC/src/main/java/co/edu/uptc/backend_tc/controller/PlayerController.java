package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.dto.filter.PlayerFilterDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.dto.response.PlayerResponseDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.repository.PlayerRepository;
import co.edu.uptc.backend_tc.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Jugadores", description = "Operaciones sobre los jugadores")
@SecurityRequirement(name = "bearerAuth")
public class PlayerController {

    private final PlayerService playerService;
    private PlayerRepository playerRepository;

    @Operation(summary = "Obtener todos los jugadores paginados")
    @GetMapping
    public ResponseEntity<PageResponseDTO<PlayerDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(playerService.getAll(pageable));
    }

    @Operation(summary = "Buscar jugadores por criterios de filtro")
    @PostMapping("/search")
    public ResponseEntity<PageResponseDTO<PlayerDTO>> search(@RequestBody PlayerFilterDTO filter, Pageable pageable) {
        return ResponseEntity.ok(playerService.search(filter, pageable));
    }

    @Operation(summary = "Obtener un jugador por ID con estadísticas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Jugador encontrado"),
        @ApiResponse(responseCode = "404", description = "Jugador no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getById(id));
    }

    @Operation(summary = "Crear un nuevo jugador")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Jugador creado exitosamente"),
        @ApiResponse(responseCode = "409", description = "Conflicto: el documento, email o código de estudiante ya existe"),
        @ApiResponse(responseCode = "400", description = "Regla de negocio no cumplida (ej. edad mínima)")
    })
    @PostMapping
    public ResponseEntity<PlayerDTO> create(@RequestBody PlayerDTO dto) {
        PlayerDTO createdPlayer = playerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @Operation(summary = "Actualizar un jugador existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Jugador actualizado"),
        @ApiResponse(responseCode = "404", description = "Jugador no encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflicto con documento o email")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PlayerDTO> update(@PathVariable Long id, @RequestBody PlayerDTO dto) {
        return ResponseEntity.ok(playerService.update(id, dto));
    }

    @Operation(summary = "Desactivar un jugador (Soft Delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Jugador desactivado"),
        @ApiResponse(responseCode = "404", description = "Jugador no encontrado"),
        @ApiResponse(responseCode = "400", description = "El jugador pertenece a equipos activos y no puede ser eliminado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<?> getByDocument(@PathVariable String documentNumber) {
        return playerService.findSummaryByDocument(documentNumber)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}