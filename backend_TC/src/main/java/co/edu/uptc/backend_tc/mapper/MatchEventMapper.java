package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.entity.Player;

public class MatchEventMapper {

    public static MatchEventDTO toDTO(MatchEvent e) {
        return MatchEventDTO.builder()
                .id(e.getId())
                .matchId(e.getMatch().getId())
                .playerId(e.getPlayer().getId())
                .type(e.getType())
                .minute(e.getMinute())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public static MatchEvent toEntity(MatchEventDTO dto, Match match, Player player) {
        return MatchEvent.builder()
                .id(dto.getId())
                .match(match)
                .player(player)
                .type(dto.getType())
                .minute(dto.getMinute())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.LocalDateTime.now())
                .build();
    }
}
