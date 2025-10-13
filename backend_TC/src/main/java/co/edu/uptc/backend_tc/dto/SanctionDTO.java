package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.SanctionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionDTO {
    private Long id;

    // Al menos uno de estos dos debe estar presente (validar en servicio)
    private Long playerId;
    private Long teamId;

    private Long matchId;

    @NotNull(message = "Sanction type is required")
    private SanctionType type;

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;

    private LocalDateTime dateIssued;
    private LocalDateTime validUntil;
}