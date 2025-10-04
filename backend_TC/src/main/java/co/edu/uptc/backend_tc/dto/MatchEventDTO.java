package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.MatchEventType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEventDTO {
    private Long id;
    private Long matchId;
    private Long playerId;
    private MatchEventType type;
    private Integer minute;
    private String description;
}
