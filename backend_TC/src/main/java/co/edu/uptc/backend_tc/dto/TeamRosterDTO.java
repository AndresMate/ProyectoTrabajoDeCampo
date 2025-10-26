package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRosterDTO {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @Min(value = 0, message = "Jersey number cannot be negative")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    private Integer jerseyNumber;

    @Builder.Default
    private Boolean isCaptain = false; // ✅ valor por defecto para evitar null
}
