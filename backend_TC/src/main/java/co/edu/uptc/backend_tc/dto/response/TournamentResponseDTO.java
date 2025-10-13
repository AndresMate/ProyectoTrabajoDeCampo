package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResponseDTO {
    private Long id;
    private String name;
    private Integer maxTeams;
    private LocalDate startDate;
    private LocalDate endDate;
    private Modality modality;
    private TournamentStatus status;
    private OffsetDateTime createdAt;

    // Información anidada
    private SportSummaryDTO sport;
    private UserSummaryDTO createdBy;

    // Estadísticas agregadas
    private Integer currentTeamCount;
    private Integer totalMatches;
    private Integer completedMatches;


}