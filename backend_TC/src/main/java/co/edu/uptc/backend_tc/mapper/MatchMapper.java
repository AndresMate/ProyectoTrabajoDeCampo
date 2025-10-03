package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchDTO;
import co.edu.uptc.backend_tc.entity.*;

public class MatchMapper {

    public static MatchDTO toDTO(Match match) {
        return MatchDTO.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .categoryId(match.getCategory().getId())
                .scenarioId(match.getScenario() != null ? match.getScenario().getId() : null)
                .startsAt(match.getStartsAt())
                .homeTeamId(match.getHomeTeam().getId())
                .awayTeamId(match.getAwayTeam().getId())
                .status(match.getStatus())
                .refereeId(match.getReferee() != null ? match.getReferee().getId() : null)
                .build();
    }

    public static Match toEntity(
            MatchDTO dto,
            Tournament tournament,
            Category category,
            Scenario scenario,
            Team homeTeam,
            Team awayTeam,
            User referee
    ) {
        return Match.builder()
                .id(dto.getId())
                .tournament(tournament)
                .category(category)
                .scenario(scenario)
                .startsAt(dto.getStartsAt())
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .status(dto.getStatus())
                .referee(referee)
                .build();
    }
}
