package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDTO {
    private Long id;
    private Long tournamentId;
    private Long categoryId;
    private Long scenarioId;
    private LocalDateTime startsAt;
    private Long homeTeamId;
    private Long awayTeamId;
    private MatchStatus status;
    private Long refereeId;
}
