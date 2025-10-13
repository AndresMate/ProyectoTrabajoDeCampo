package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.dto.response.TeamRosterResponseDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamRoster;
import org.springframework.stereotype.Component;

@Component
public class TeamRosterMapper {

    private final PlayerMapper playerMapper;

    public TeamRosterMapper(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public TeamRosterDTO toDTO(TeamRoster entity) {
        if (entity == null) return null;

        return TeamRosterDTO.builder()
                .teamId(entity.getTeam() != null ? entity.getTeam().getId() : null)
                .playerId(entity.getPlayer() != null ? entity.getPlayer().getId() : null)
                .jerseyNumber(entity.getJerseyNumber())
                .isCaptain(entity.getIsCaptain())
                .build();
    }

    public TeamRosterResponseDTO toResponseDTO(TeamRoster entity) {
        if (entity == null) return null;

        return TeamRosterResponseDTO.builder()
                .player(playerMapper.toSummaryDTO(entity.getPlayer()))
                .jerseyNumber(entity.getJerseyNumber())
                .isCaptain(entity.getIsCaptain())
                .build();
    }
}
