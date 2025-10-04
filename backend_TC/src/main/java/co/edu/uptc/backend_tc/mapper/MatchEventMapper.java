package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.entity.Player;

public class MatchEventMapper {

    public static MatchEventDTO toDTO(MatchEvent event) {
        return MatchEventDTO.builder()
                .id(event.getId())
                .matchId(event.getMatch().getId())
                .playerId(event.getPlayer().getId())
                .type(event.getType())
                .minute(event.getMinute())
                .description(event.getDescription())
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
                .build();
    }
}
