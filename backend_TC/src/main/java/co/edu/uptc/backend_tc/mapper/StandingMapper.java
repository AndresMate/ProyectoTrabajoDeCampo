package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.StandingDTO;
import co.edu.uptc.backend_tc.entity.Standing;

public class StandingMapper {

    public static StandingDTO toDTO(Standing s) {
        return StandingDTO.builder()
                .id(s.getId())
                .tournamentId(s.getTournament().getId())
                .categoryId(s.getCategory().getId())
                .teamId(s.getTeam().getId())
                .points(s.getPoints())
                .played(s.getPlayed())
                .wins(s.getWins())
                .draws(s.getDraws())
                .losses(s.getLosses())
                .goalsFor(s.getGoalsFor())
                .goalsAgainst(s.getGoalsAgainst())
                .build();
    }
}
