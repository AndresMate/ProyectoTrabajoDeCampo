package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamSummaryDTO {
    private Long id;
    private String name;
    private ClubSummaryDTO club;
}