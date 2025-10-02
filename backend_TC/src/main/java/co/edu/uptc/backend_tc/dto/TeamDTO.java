package co.edu.uptc.backend_tc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDTO {
    private Long id;
    private String name;
    private Boolean isActive;
    private Long tournamentId;
    private Long categoryId;
    private Long originInscriptionId;
    private Long clubId;
}
