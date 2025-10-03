package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.*;

public class MatchResultMapper {

    public static MatchResultDTO toDTO(MatchResult mr) {
        return MatchResultDTO.builder()
                .matchId(mr.getMatch().getId())
                .homeScore(mr.getHomeScore())
                .awayScore(mr.getAwayScore())
                .notes(mr.getNotes())
                .enteredById(mr.getEnteredBy().getId())
                .enteredAt(mr.getEnteredAt())
                .validatedById(mr.getValidatedBy() != null ? mr.getValidatedBy().getId() : null)
                .validatedAt(mr.getValidatedAt())
                .build();
    }

    public static MatchResult toEntity(
            MatchResultDTO dto,
            Match match,
            User enteredBy,
            User validatedBy
    ) {
        return MatchResult.builder()
                .match(match)
                .homeScore(dto.getHomeScore())
                .awayScore(dto.getAwayScore())
                .notes(dto.getNotes())
                .enteredBy(enteredBy)
                .enteredAt(dto.getEnteredAt() != null ? dto.getEnteredAt() : java.time.LocalDateTime.now())
                .validatedBy(validatedBy)
                .validatedAt(dto.getValidatedAt())
                .build();
    }
}
