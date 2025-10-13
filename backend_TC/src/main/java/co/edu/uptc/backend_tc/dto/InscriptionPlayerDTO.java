package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InscriptionPlayerDTO {

    @NotNull(message = "Inscription ID is required")
    private Long inscriptionId;

    @NotNull(message = "Player ID is required")
    private Long playerId;
}