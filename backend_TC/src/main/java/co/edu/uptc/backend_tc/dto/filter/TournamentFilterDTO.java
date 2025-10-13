package co.edu.uptc.backend_tc.dto.filter;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentFilterDTO {
    private String name;
    private TournamentStatus status;
    private Modality modality;
    private Long sportId;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private Long createdById;
}