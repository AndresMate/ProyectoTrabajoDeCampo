package co.edu.uptc.backend_tc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscriptionPlayerDTO {
    private Long inscriptionId;
    private Long playerId;
}
