package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscriptionResponseDTO {
    private Long id;
    private String teamName;
    private String delegatePhone;
    private InscriptionStatus status;
    private OffsetDateTime createdAt;

    // Información anidada
    private TournamentSummaryDTO tournament;
    private CategorySummaryDTO category;
    private PlayerSummaryDTO delegate;
    private ClubSummaryDTO club;

    // Lista de jugadores inscritos
    private List<PlayerSummaryDTO> players;
    private Integer playerCount;
}