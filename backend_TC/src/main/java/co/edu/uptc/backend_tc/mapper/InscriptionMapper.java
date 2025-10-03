package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.InscriptionDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.Player;
import co.edu.uptc.backend_tc.entity.Tournament;

public class InscriptionMapper {

    public static InscriptionDTO toDTO(Inscription i) {
        return InscriptionDTO.builder()
                .id(i.getId())
                .teamName(i.getTeamName())
                .delegatePhone(i.getDelegatePhone())
                .status(i.getStatus())
                .tournamentId(i.getTournament().getId())
                .categoryId(i.getCategory().getId())
                .delegatePlayerId(i.getDelegate().getId())
                .build();
    }

    public static Inscription toEntity(InscriptionDTO dto, Tournament tournament, Category category, Player delegate) {
        return Inscription.builder()
                .id(dto.getId())
                .teamName(dto.getTeamName())
                .delegatePhone(dto.getDelegatePhone())
                .status(dto.getStatus())
                .tournament(tournament)
                .category(category)
                .delegate(delegate)
                .build();
    }
}
