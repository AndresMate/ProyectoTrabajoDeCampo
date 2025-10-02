package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}
