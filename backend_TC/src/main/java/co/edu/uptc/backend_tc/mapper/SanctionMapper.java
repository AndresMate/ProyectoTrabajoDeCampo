package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.entity.*;

public class SanctionMapper {

    public static SanctionDTO toDTO(Sanction s) {
        return SanctionDTO.builder()
                .id(s.getId())
                .playerId(s.getPlayer() != null ? s.getPlayer().getId() : null)
                .teamId(s.getTeam() != null ? s.getTeam().getId() : null)
                .matchId(s.getMatch() != null ? s.getMatch().getId() : null)
                .type(s.getType())
                .reason(s.getReason())
                .dateIssued(s.getDateIssued())
                .validUntil(s.getValidUntil())
                .build();
    }

    public static Sanction toEntity(SanctionDTO dto, Player player, Team team, Match match) {
        return Sanction.builder()
                .id(dto.getId())
                .player(player)
                .team(team)
                .match(match)
                .type(dto.getType())
                .reason(dto.getReason())
                .dateIssued(dto.getDateIssued() != null ? dto.getDateIssued() : java.time.LocalDateTime.now())
                .validUntil(dto.getValidUntil())
                .build();
    }
}
