package co.edu.uptc.backend_tc.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentStatsDTO {

    private Long totalTournaments;
    private Long planningCount;
    private Long openForInscriptionCount;
    private Long inProgressCount;
    private Long finishedCount;
    private Long cancelledCount;

    @Builder.Default
    private Map<String, Long> tournamentsBySport = Map.of();

    @Builder.Default
    private Map<String, Long> tournamentsByModality = Map.of();

    @Builder.Default
    private Map<String, Long> tournamentsByStatus = Map.of();

    private Double completionRate;
    private Long activePlayersCount;
    private Long totalMatchesPlayed;
    private Long tournamentsThisMonth;
    private Long tournamentsThisYear;
}