package co.edu.uptc.backend_tc.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResultDTO {
    private Long matchId;
    private Integer homeScore;
    private Integer awayScore;
    private String notes;
    private Long enteredById;
    private OffsetDateTime enteredAt;
    private Long validatedById;
    private OffsetDateTime validatedAt;
}
