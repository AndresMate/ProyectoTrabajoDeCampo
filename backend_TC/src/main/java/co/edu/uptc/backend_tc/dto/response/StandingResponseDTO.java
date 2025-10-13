package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandingResponseDTO {
    private Long id;
    private Integer position; // posición en la tabla
    private TeamSummaryDTO team;
    private Integer points;
    private Integer played;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;

    // Últimos resultados (opcional)
    private String form; // ej: "WWDLW"
}
