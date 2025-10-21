package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.dto.response.InscriptionResponseDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Tournament;
import org.springframework.stereotype.Component;

@Component
public class InscriptionMapper {

    private final TournamentMapper tournamentMapper;
    private final CategoryMapper categoryMapper;
    private final PlayerMapper playerMapper;
    private final ClubMapper clubMapper;

    public InscriptionMapper(TournamentMapper tournamentMapper,
                             CategoryMapper categoryMapper,
                             PlayerMapper playerMapper,
                             ClubMapper clubMapper) {
        this.tournamentMapper = tournamentMapper;
        this.categoryMapper = categoryMapper;
        this.playerMapper = playerMapper;
        this.clubMapper = clubMapper;
    }

    public InscriptionDTO toDTO(Inscription entity) {
        if (entity == null) return null;

        return InscriptionDTO.builder()
                .id(entity.getId())
                .teamName(entity.getTeamName())
                .delegatePhone(entity.getDelegatePhone())
                .status(entity.getStatus())
                .tournamentId(entity.getTournament() != null ? entity.getTournament().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .delegateIndex(Math.toIntExact(entity.getDelegate() != null ? entity.getDelegate().getId() : null))
                .clubId(entity.getClub() != null ? entity.getClub().getId() : null)
                .build();
    }

    public InscriptionResponseDTO toResponseDTO(Inscription entity) {
        if (entity == null) return null;

        return InscriptionResponseDTO.builder()
                .id(entity.getId())
                .teamName(entity.getTeamName())
                .delegatePhone(entity.getDelegatePhone())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .tournament(tournamentMapper.toSummaryDTO(entity.getTournament()))
                .category(categoryMapper.toSummaryDTO(entity.getCategory()))
                .delegate(playerMapper.toSummaryDTO(entity.getDelegate()))
                .club(clubMapper.toSummaryDTO(entity.getClub()))
                // players se agregarían desde el servicio
                .build();
    }
}