package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResultSummaryDTO {
    private Integer homeScore;
    private Integer awayScore;
    private String notes;
}