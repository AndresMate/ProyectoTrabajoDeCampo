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
                .wins(entity.getWins())
                .draws(entity.getDraws())
                .losses(entity.getLosses())
                .goalsFor(entity.getGoalsFor())
                .goalsAgainst(entity.getGoalsAgainst())
                .goalDifference(entity.getGoalDifference())
                .build();
    }

    public StandingResponseDTO toResponseDTO(Standing entity, Integer position, String form) {
        if (entity == null) return null;

        return StandingResponseDTO.builder()
                .id(entity.getId())
                .position(position)
                .team(teamMapper.toSummaryDTO(entity.getTeam()))
                .points(entity.getPoints())
                .played(entity.getPlayed())
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