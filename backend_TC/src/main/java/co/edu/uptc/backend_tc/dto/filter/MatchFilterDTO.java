package co.edu.uptc.backend_tc.dto.filter;

import co.edu.uptc.backend_tc.model.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchFilterDTO {
    private Long tournamentId;
    private Long categoryId;
    private Long teamId;
    private Long scenarioId;
    private MatchStatus status;
    private LocalDateTime startDateFrom;
    private LocalDateTime startDateTo;
    private Long refereeId;
}