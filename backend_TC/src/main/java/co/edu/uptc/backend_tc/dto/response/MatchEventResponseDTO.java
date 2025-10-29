package co.edu.uptc.backend_tc.dto.response;

import co.edu.uptc.backend_tc.model.MatchEventType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEventResponseDTO {
    private Long id;
    private MatchEventType type;
    private Integer minute;
    private String description;
    private LocalDateTime createdAt;

    // Información anidada
    private PlayerSummaryDTO player;
    private TeamSummaryDTO team;
}