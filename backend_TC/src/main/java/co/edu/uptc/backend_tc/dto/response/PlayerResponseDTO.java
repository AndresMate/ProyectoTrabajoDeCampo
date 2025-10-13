package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponseDTO {
    private Long id;
    private String fullName;
    private String studentCode;
    private String documentNumber;
    private String institutionalEmail;
    private LocalDate birthDate;
    private Integer age; // calculado
    private Boolean isActive;

    // Estadísticas
    private Integer teamsCount;
    private Integer totalMatches;
    private Integer goals;
    private Integer yellowCards;
    private Integer redCards;
}
