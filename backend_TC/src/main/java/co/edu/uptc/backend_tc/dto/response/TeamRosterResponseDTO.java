package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRosterResponseDTO {

    private PlayerSummaryDTO player;

    private Integer jerseyNumber;

    @Builder.Default
    private Boolean isCaptain = false; // ✅ Evita valores null en respuestas
}
