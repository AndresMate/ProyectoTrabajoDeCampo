package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.TournamentStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentSummaryDTO {
    private Long id;
    private String name;
    private TournamentStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}