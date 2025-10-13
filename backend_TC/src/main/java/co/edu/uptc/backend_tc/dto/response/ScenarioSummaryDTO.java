package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioSummaryDTO {
    private Long id;
    private String name;
    private VenueSummaryDTO venue;
}