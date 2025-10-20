package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummaryDTO {
    private Long id;
    private String name;
    private Integer membersPerTeam;
}