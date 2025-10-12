package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchResult;

public class MatchResultMapper {

    public static MatchResultDTO toDTO(MatchResult r) {
        return MatchResultDTO.builder()
                .matchId(r.getMatch().getId())
                .homeScore(r.getHomeScore())
                .awayScore(r.getAwayScore())
                .notes(r.getNotes())
                .enteredAt(r.getEnteredAt())
                .build();
    }

    public static MatchResult toEntity(MatchResultDTO dto, Match match) {
        return MatchResult.builder()
                .match(match)
                .homeScore(dto.getHomeScore())
                .awayScore(dto.getAwayScore())
                .notes(dto.getNotes())
                .enteredAt(dto.getEnteredAt() != null ? dto.getEnteredAt() : java.time.LocalDateTime.now())
                .build();
    }
}
