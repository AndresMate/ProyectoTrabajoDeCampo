package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchSummaryDTO {
    private Long id;
    private LocalDateTime startsAt;
    private MatchStatus status;
    private TeamSummaryDTO homeTeam;
    private TeamSummaryDTO awayTeam;
}