package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.SanctionDTO;
import co.edu.uptc.backend_tc.dto.response.SanctionResponseDTO;
import co.edu.uptc.backend_tc.entity.*;
import org.springframework.stereotype.Component;

@Component
public class SanctionMapper {

    private final PlayerMapper playerMapper;
    private final TeamMapper teamMapper;
    private final MatchMapper matchMapper;

    public SanctionMapper(PlayerMapper playerMapper,
                          TeamMapper teamMapper,
                          MatchMapper matchMapper) {
        this.playerMapper = playerMapper;
        this.teamMapper = teamMapper;
        this.matchMapper = matchMapper;
    }

    public static SanctionDTO toDTO(Sanction entity) {
        if (entity == null) return null;

        return SanctionDTO.builder()
                .id(entity.getId())
                .playerId(entity.getPlayer() != null ? entity.getPlayer().getId() : null)
                .teamId(entity.getTeam() != null ? entity.getTeam().getId() : null)
                .matchId(entity.getMatch() != null ? entity.getMatch().getId() : null)
                .type(entity.getType())
                .reason(entity.getReason())
                .dateIssued(entity.getDateIssued())
                .validUntil(entity.getValidUntil())
                .build();
    }

    public SanctionResponseDTO toResponseDTO(Sanction entity) {
        if (entity == null) return null;

        return SanctionResponseDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .reason(entity.getReason())
                .dateIssued(entity.getDateIssued())
                .validUntil(entity.getValidUntil())
                .isActive(entity.isActive())
                .player(playerMapper.toSummaryDTO(entity.getPlayer()))
                .team(teamMapper.toSummaryDTO(entity.getTeam()))
                .match(matchMapper.toSummaryDTO(entity.getMatch()))
                .build();
    }
    public static Sanction toEntity(SanctionDTO dto, Player player, Team team, Match match) {
        Sanction sanction = new Sanction();
        sanction.setId(dto.getId());
        sanction.setReason(dto.getReason());
        sanction.setPlayer(player);
        sanction.setTeam(team);
        sanction.setMatch(match);
        sanction.setDateIssued(dto.getDateIssued());
        return sanction;
    }
}