package co.edu.uptc.backend_tc.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseDTO {
    private Long id;
    private String name;
    private Boolean isActive;

    // Información anidada
    private TournamentSummaryDTO tournament;
    private CategorySummaryDTO category;
    private ClubSummaryDTO club;

    // Roster
    private List<TeamRosterResponseDTO> roster;
    private Integer rosterSize;

    // Estadísticas
    private Integer matchesPlayed;
    private Integer wins;
    private Integer draws;
    private Integer losses;
}