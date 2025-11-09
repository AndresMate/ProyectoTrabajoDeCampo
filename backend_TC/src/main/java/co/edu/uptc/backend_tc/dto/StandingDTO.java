package co.edu.uptc.backend_tc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandingDTO {
    private Long id;
    private Long tournamentId;
    private Long categoryId;
    private Long teamId;
    private String teamName; // ✅ Nombre del equipo
    private Integer points;
    private Integer played;
    private Integer matchesPlayed; // Alias para played
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer goalDifference;
}