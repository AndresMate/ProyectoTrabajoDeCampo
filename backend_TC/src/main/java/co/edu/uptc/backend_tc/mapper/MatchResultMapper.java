package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchResult;

public class MatchResultMapper {

    public static MatchResultDTO toDTO(MatchResult result) {
        return MatchResultDTO.builder()
                .matchId(result.getMatch().getId())
                .homeScore(result.getHomeScore())
                .awayScore(result.getAwayScore())
                .notes(result.getNotes())
                .enteredAt(result.getEnteredAt())
                .build();
    }

    public static MatchResult toEntity(MatchResultDTO dto, Match match) {
        return MatchResult.builder()
                .match(match)
                .homeScore(dto.getHomeScore())
                .awayScore(dto.getAwayScore())
                .notes(dto.getNotes())
                .build();
    }
}
