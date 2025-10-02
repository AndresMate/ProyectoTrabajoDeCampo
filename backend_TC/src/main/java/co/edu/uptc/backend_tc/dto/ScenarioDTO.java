package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioDTO {
    private Long id;
    private String name;
    private Integer capacity;
    private Boolean supportsNightGames;
    private Long venueId;
}
