package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerSummaryDTO {
    private Long id;
    private String fullName;
    private String documentNumber;
}