package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.InscriptionPlayerDTO;
import co.edu.uptc.backend_tc.service.InscriptionPlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Se anidan las rutas para mayor claridad REST: /api/inscriptions/{inscriptionId}/players
@RequestMapping("/api/inscriptions/{inscriptionId}/players")
@RequiredArgsConstructor
@Tag(name = "Inscripciones - Jugadores", description = "Operaciones para gestionar los jugadores de una inscripción")
public class InscriptionPlayerController {

    private final InscriptionPlayerService inscriptionPlayerService;

    @Operation(summary = "Obtener los jugadores de una inscripción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de jugadores encontrada"),
        @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @GetMapping
    public ResponseEntity<List<InscriptionPlayerDTO>> getPlayersByInscription(@PathVariable Long inscriptionId) {
        return ResponseEntity.ok(inscriptionPlayerService.getByInscription(inscriptionId));
    }

    @Operation(summary = "Añadir un jugador a una inscripción")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Jugador añadido exitosamente"),
        @ApiResponse(responseCode = "400", description = "La inscripción no está en estado PENDIENTE o se alcanzó el límite de jugadores"),
        @ApiResponse(responseCode = "404", description = "Inscripción o jugador no encontrado"),
        @ApiResponse(responseCode = "409", description = "El jugador ya está en la inscripción")
    })
    @PostMapping
    public ResponseEntity<InscriptionPlayerDTO> addPlayer(
            @PathVariable Long inscriptionId,
            @RequestBody InscriptionPlayerDTO requestDto) { // Se espera un cuerpo con { "playerId": 123 }
        
        // Se asegura que el ID de la inscripción sea el de la URL
        requestDto.setInscriptionId(inscriptionId);
        
        InscriptionPlayerDTO addedPlayer = inscriptionPlayerService.addPlayerToInscription(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedPlayer);
    }
    
    @Operation(summary = "Eliminar un jugador de una inscripción")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Jugador eliminado exitosamente"),
        @ApiResponse(responseCode = "400", description = "La inscripción no está en estado PENDIENTE"),
        @ApiResponse(responseCode = "404", description = "La relación jugador-inscripción no existe")
    })
    @DeleteMapping("/{playerId}")
    public ResponseEntity<Void> removePlayer(
            @PathVariable Long inscriptionId,
            @PathVariable Long playerId) {
        inscriptionPlayerService.removePlayerFromInscription(inscriptionId, playerId);
        return ResponseEntity.noContent().build();
    }
}