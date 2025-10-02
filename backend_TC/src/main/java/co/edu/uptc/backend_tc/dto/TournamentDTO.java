package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.Modality;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDTO {
    private Long id;
    private String name;
    private Integer maxTeams;
    private LocalDate startDate;
    private LocalDate endDate;
    private Modality modality;
    private TournamentStatus status;
    private Long sportId;
    private Long createdById;
}
