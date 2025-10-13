package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueSummaryDTO {
    private Long id;
    private String name;
    private String address;
}