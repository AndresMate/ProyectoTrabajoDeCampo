package co.edu.uptc.backend_tc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandingResponseDTO {
    private Long id;
    private Integer position;

    // ✅ CRÍTICO: Campo que el frontend necesita
    private String teamName;

    // Información completa del equipo
    private TeamSummaryDTO team;

    // Estadísticas del equipo
    private Integer points;
    private Integer played;
    private Integer matchesPlayed; // Alias para 'played' (compatibilidad con frontend)
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;

    // Forma reciente (últimos 5 partidos) - Ejemplo: "WWDLW"
    private String form;
}