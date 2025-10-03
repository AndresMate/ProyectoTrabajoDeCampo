package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRosterDTO {
    private Long teamId;
    private Long playerId;
    private Integer jerseyNumber;
    private Boolean captain; // 🔹 igual que en la entidad
}
