package co.edu.uptc.backend_tc.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResultDTO {

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotNull(message = "Home score is required")
    @Min(value = 0, message = "Home score must be >= 0")
    private Integer homeScore;

    @NotNull(message = "Away score is required")
    @Min(value = 0, message = "Away score must be >= 0")
    private Integer awayScore;

    @Size(max = 1000, message = "Notes can have at most 1000 characters")
    private String notes;

    private LocalDateTime enteredAt;
}