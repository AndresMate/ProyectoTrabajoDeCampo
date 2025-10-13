package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDTO {
    private Long id;
    private LocalDateTime startsAt;
    private MatchStatus status;

    // Información anidada
    private TournamentSummaryDTO tournament;
    private CategorySummaryDTO category;
    private ScenarioSummaryDTO scenario;
    private TeamSummaryDTO homeTeam;
    private TeamSummaryDTO awayTeam;
    private UserSummaryDTO referee;

    // Resultado (si existe)
    private MatchResultSummaryDTO result;

    // Estadísticas
    private Integer eventCount;
}