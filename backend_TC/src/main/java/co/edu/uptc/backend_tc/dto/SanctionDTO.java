package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionDTO {
    private Long id;
    private String description;
    private String sanctionType;
    private Long playerId;
    private Long matchId;
}
