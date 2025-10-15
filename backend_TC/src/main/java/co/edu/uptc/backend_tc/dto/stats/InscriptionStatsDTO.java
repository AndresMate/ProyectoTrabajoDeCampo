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
public class InscriptionStatsDTO {

    private Long totalInscriptions;
    private Long pendingCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long cancelledCount;

    @Builder.Default
    private Map<String, Long> inscriptionsByTournament = Map.of();

    @Builder.Default
    private Map<String, Long> inscriptionsBySport = Map.of();

    @Builder.Default
    private Map<String, Long> inscriptionsByCategory = Map.of();

    private Double approvalRate;
    private Long inscriptionsLastWeek;
    private Long inscriptionsLastMonth;
}