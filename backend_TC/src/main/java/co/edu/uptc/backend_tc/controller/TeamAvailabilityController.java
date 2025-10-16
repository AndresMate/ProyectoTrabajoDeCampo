package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.TeamAvailabilityDTO;
import co.edu.uptc.backend_tc.service.TeamAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-availability")
@RequiredArgsConstructor
@Tag(name = "Disponibilidad de Equipos", description = "Gestión de la disponibilidad horaria de los equipos")
@SecurityRequirement(name = "bearerAuth")
public class TeamAvailabilityController {

    private final TeamAvailabilityService availabilityService;

    @Operation(summary = "Obtener la disponibilidad de un equipo")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamAvailabilityDTO>> getByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(availabilityService.getByTeam(teamId));
    }

    @Operation(summary = "Guardar o actualizar la disponibilidad de un equipo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilidad guardada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos (horarios solapados, días faltantes, rangos incorrectos)"),
        @ApiResponse(responseCode = "404", description = "Equipo no encontrado")
    })
    @PostMapping("/team/{teamId}")
    public ResponseEntity<List<TeamAvailabilityDTO>> saveAvailabilities(
            @PathVariable Long teamId,
            @RequestParam boolean isNocturno,
            @RequestBody List<TeamAvailabilityDTO> dtoList) {
        List<TeamAvailabilityDTO> savedAvailabilities = availabilityService.saveAvailabilities(teamId, dtoList, isNocturno);
        return ResponseEntity.ok(savedAvailabilities);
    }
}