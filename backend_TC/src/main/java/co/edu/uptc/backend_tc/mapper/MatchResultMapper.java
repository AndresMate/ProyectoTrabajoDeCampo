package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchResultDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class MatchResultMapper {

    public MatchResultDTO toDTO(MatchResult entity) {
        if (entity == null) return null;

        return MatchResultDTO.builder()
                .matchId(entity.getMatchId())  // ✅ CORRECTO: Usar getMatchId()
                .homeScore(entity.getHomeScore())
                .awayScore(entity.getAwayScore())
                .notes(entity.getNotes())
                .enteredAt(entity.getEnteredAt())
                .build();
    }

    public MatchResult toEntity(MatchResultDTO dto, Match match) {
        if (dto == null) return null;

        return MatchResult.builder()
                .match(match)  // ✅ El matchId se genera automáticamente por @MapsId
                .homeScore(dto.getHomeScore())
                .awayScore(dto.getAwayScore())
                .notes(dto.getNotes())
                .build();
    }

    public void updateEntityFromDTO(MatchResultDTO dto, MatchResult entity) {
        if (dto.getHomeScore() != null) {
            entity.setHomeScore(dto.getHomeScore());
        }
        if (dto.getAwayScore() != null) {
            entity.setAwayScore(dto.getAwayScore());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}