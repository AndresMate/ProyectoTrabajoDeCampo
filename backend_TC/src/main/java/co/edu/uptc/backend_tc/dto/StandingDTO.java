package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandingDTO {
    private Long tournamentId;
    private Long categoryId;
    private Long teamId;
    private Integer points;
    private Integer played;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalsFor;
    private Integer goalsAgainst;
}
