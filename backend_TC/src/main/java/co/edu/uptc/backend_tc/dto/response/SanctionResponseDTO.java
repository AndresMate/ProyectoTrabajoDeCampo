package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.SanctionType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionResponseDTO {
    private Long id;
    private SanctionType type;
    private String reason;
    private LocalDateTime dateIssued;
    private LocalDateTime validUntil;
    private Boolean isActive; // calculado

    // Información anidada
    private PlayerSummaryDTO player;
    private TeamSummaryDTO team;
    private MatchSummaryDTO match;
}