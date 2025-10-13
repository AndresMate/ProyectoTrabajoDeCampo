package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandingDTO {
    private Long id;
    private String teamName; // para response
    private Long tournamentId;
    private Long categoryId;
    private Long teamId;

    @Min(value = 0, message = "Points cannot be negative")
    private Integer points;

    @Min(value = 0, message = "Played cannot be negative")
    private Integer played;

    @Min(value = 0, message = "Wins cannot be negative")
    private Integer wins;

    @Min(value = 0, message = "Draws cannot be negative")
    private Integer draws;

    @Min(value = 0, message = "Losses cannot be negative")
    private Integer losses;

    @Min(value = 0, message = "Goals for cannot be negative")
    private Integer goalsFor;

    @Min(value = 0, message = "Goals against cannot be negative")
    private Integer goalsAgainst;

    // Campo calculado para response
    private Integer goalDifference;
}