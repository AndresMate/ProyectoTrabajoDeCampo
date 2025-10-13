package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.MatchStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDTO {
    private Long id;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long scenarioId;

    @Future(message = "Match date must be in the future")
    private LocalDateTime startsAt;

    @NotNull(message = "Home team ID is required")
    private Long homeTeamId;

    @NotNull(message = "Away team ID is required")
    private Long awayTeamId;

    private MatchStatus status;

    private Long refereeId;
}