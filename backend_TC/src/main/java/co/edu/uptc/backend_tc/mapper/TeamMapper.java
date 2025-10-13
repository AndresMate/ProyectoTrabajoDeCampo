package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.TeamDTO;
import co.edu.uptc.backend_tc.dto.response.TeamResponseDTO;
import co.edu.uptc.backend_tc.dto.response.TeamSummaryDTO;
import co.edu.uptc.backend_tc.entity.*;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    private final TournamentMapper tournamentMapper;
    private final CategoryMapper categoryMapper;
    private final ClubMapper clubMapper;

    public TeamMapper(TournamentMapper tournamentMapper,
                      CategoryMapper categoryMapper,
                      ClubMapper clubMapper) {
        this.tournamentMapper = tournamentMapper;
        this.categoryMapper = categoryMapper;
        this.clubMapper = clubMapper;
    }

    public TeamDTO toDTO(Team entity) {
        if (entity == null) return null;

        return TeamDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .tournamentId(entity.getTournament() != null ? entity.getTournament().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .originInscriptionId(entity.getOriginInscription() != null ? entity.getOriginInscription().getId() : null)
                .clubId(entity.getClub() != null ? entity.getClub().getId() : null)
                .build();
    }

    public TeamResponseDTO toResponseDTO(Team entity) {
        if (entity == null) return null;

        return TeamResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isActive(entity.getIsActive())
                .tournament(tournamentMapper.toSummaryDTO(entity.getTournament()))
                .category(categoryMapper.toSummaryDTO(entity.getCategory()))
                .club(clubMapper.toSummaryDTO(entity.getClub()))
                // roster y estadísticas se agregan desde el servicio
                .build();
    }

    public TeamSummaryDTO toSummaryDTO(Team entity) {
        if (entity == null) return null;

        return TeamSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .club(clubMapper.toSummaryDTO(entity.getClub()))
                .build();
    }
}
