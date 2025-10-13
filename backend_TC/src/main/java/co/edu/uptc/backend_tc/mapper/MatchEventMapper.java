package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.MatchEventDTO;
import co.edu.uptc.backend_tc.dto.response.MatchEventResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamSummaryDTO;
import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.entity.Player;
import org.springframework.stereotype.Component;

@Component
public class MatchEventMapper {

    private final PlayerMapper playerMapper;

    public MatchEventMapper(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public MatchEventDTO toDTO(MatchEvent entity) {
        if (entity == null) return null;

        return MatchEventDTO.builder()
                .id(entity.getId())
                .matchId(entity.getMatch() != null ? entity.getMatch().getId() : null)
                .playerId(entity.getPlayer() != null ? entity.getPlayer().getId() : null)
                .type(entity.getType())
                .minute(entity.getMinute())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public MatchEvent toEntity(MatchEventDTO dto, Match match, Player player) {
        if (dto == null) return null;

        return MatchEvent.builder()
                .id(dto.getId())
                .match(match)
                .player(player)
                .type(dto.getType())
                .minute(dto.getMinute())
                .description(dto.getDescription())
                .build();
    }

    public MatchEventResponseDTO toResponseDTO(MatchEvent entity, TeamSummaryDTO team) {
        if (entity == null) return null;

        return MatchEventResponseDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .minute(entity.getMinute())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .player(playerMapper.toSummaryDTO(entity.getPlayer()))
                .team(team)
                .build();
    }
}