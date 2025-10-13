package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubSummaryDTO {
    private Long id;
    private String name;
}