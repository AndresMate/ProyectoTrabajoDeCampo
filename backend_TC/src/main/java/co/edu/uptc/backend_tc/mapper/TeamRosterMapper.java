package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamRosterDTO;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Team;
import co.edu.uptc.backend_tc.entity.TeamRoster;

public class TeamRosterMapper {

    public static TeamRosterDTO toDTO(TeamRoster tr) {
        return TeamRosterDTO.builder()
                .teamId(tr.getTeam().getId())
                .playerId(tr.getPlayer().getId())
                .jerseyNumber(tr.getJerseyNumber())
                .captain(tr.getCaptain()) // 🔹 ahora coincide
                .build();
    }

    public static TeamRoster toEntity(TeamRosterDTO dto, Team team, Player player) {
        return TeamRoster.builder()
                .team(team)
                .player(player)
                .jerseyNumber(dto.getJerseyNumber())
                .captain(dto.getCaptain()) // 🔹 ahora coincide
                .build();
    }
}
