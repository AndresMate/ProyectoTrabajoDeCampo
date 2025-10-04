package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.SanctionType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionDTO {
    private Long id;
    private Long playerId;
    private Long teamId;
    private Long matchId;
    private SanctionType type;
    private String reason;
    private LocalDateTime dateIssued;
    private LocalDateTime validUntil;
}
