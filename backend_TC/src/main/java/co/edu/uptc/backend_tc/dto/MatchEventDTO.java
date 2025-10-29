package co.edu.uptc.backend_tc.dto;

import co.edu.uptc.backend_tc.model.MatchEventType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEventDTO {
    private Long id;

    @NotNull(message = "Match ID is required")
    private Long matchId;

    private Long teamId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Event type is required")
    private MatchEventType type;

    @Min(value = 0, message = "Minute cannot be negative")
    @Max(value = 200, message = "Minute cannot exceed 200")
    private Integer minute;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private LocalDateTime createdAt;
}

