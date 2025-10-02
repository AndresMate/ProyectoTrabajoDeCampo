package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.MatchStatus;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDTO {
    private Long id;
    private OffsetDateTime startsAt;
    private MatchStatus status;
    private Long tournamentId;
    private Long categoryId;
    private Long scenarioId;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long refereeId;
}
