package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.entity.*;

public class TeamMapper {

    public static TeamDTO toDTO(Team t) {
        return TeamDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .isActive(t.getIsActive())
                .tournamentId(t.getTournament().getId())
                .categoryId(t.getCategory().getId())
                .originInscriptionId(t.getOriginInscription() != null ? t.getOriginInscription().getId() : null)
                .clubId(t.getClub() != null ? t.getClub().getId() : null)
                .build();
    }

    public static Team toEntity(TeamDTO dto, Tournament tournament, Category category, Inscription inscription, Club club) {
        return Team.builder()
                .id(dto.getId())
                .name(dto.getName())
                .isActive(dto.getIsActive())
                .tournament(tournament)
                .category(category)
                .originInscription(inscription)
                .club(club)
                .build();
    }
}
