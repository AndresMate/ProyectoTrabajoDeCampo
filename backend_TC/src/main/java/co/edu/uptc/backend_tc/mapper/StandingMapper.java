package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.StandingDTO;
import co.edu.uptc.backend_tc.dto.response.StandingResponseDTO;
import co.edu.uptc.backend_tc.entity.Standing;
import org.springframework.stereotype.Component;

@Component
public class StandingMapper {

    private final TeamMapper teamMapper;

    public StandingMapper(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public StandingDTO toDTO(Standing entity) {
        if (entity == null) return null;

        return StandingDTO.builder()
                .id(entity.getId())
                .teamName(entity.getTeam() != null ? entity.getTeam().getName() : null)
                .tournamentId(entity.getTournament() != null ? entity.getTournament().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .teamId(entity.getTeam() != null ? entity.getTeam().getId() : null)
                .points(entity.getPoints())
                .played(entity.getPlayed())
                .matchesPlayed(entity.getPlayed())
                .wins(entity.getWins())
                .draws(entity.getDraws())
                .losses(entity.getLosses())
                .goalsFor(entity.getGoalsFor())
                .goalsAgainst(entity.getGoalsAgainst())
                .goalDifference(entity.getGoalDifference())
                .build();
    }

    /**
     * ✅ MÉTODO CORREGIDO: Incluye teamName y matchesPlayed
     */
    public StandingResponseDTO toResponseDTO(Standing entity, Integer position, String form) {
        if (entity == null) return null;

        return StandingResponseDTO.builder()
                .id(entity.getId())
                .position(position != null ? position : 0)
                // ✅ CRÍTICO: teamName para que el frontend pueda mostrar el nombre
                .teamName(entity.getTeam() != null ? entity.getTeam().getName() : "Equipo Desconocido")
                // Información completa del equipo (opcional)
                .team(teamMapper.toSummaryDTO(entity.getTeam()))
                // Estadísticas
                .points(entity.getPoints())
                .played(entity.getPlayed())
                .matchesPlayed(entity.getPlayed()) // ✅ Alias para compatibilidad
                .wins(entity.getWins())
                .draws(entity.getDraws())
                .losses(entity.getLosses())
                .goalsFor(entity.getGoalsFor())
                .goalsAgainst(entity.getGoalsAgainst())
                .goalDifference(entity.getGoalDifference())
                .form(form)
                .build();
    }
}