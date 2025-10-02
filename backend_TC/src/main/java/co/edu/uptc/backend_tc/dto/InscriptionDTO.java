package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.InscriptionStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscriptionDTO {
    private Long id;
    private String teamName;
    private String delegatePhone;
    private InscriptionStatus status;
    private Long tournamentId;
    private Long categoryId;
    private Long delegatePlayerId;
}
