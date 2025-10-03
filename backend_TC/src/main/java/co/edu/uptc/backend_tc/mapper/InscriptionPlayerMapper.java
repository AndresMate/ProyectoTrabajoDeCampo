package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.InscriptionPlayerDTO;
import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import co.edu.uptc.backend_tc.entity.Player;

public class InscriptionPlayerMapper {

    public static InscriptionPlayerDTO toDTO(InscriptionPlayer ip) {
        return InscriptionPlayerDTO.builder()
                .inscriptionId(ip.getInscription().getId())
                .playerId(ip.getPlayer().getId())
                .build();
    }

    public static InscriptionPlayer toEntity(InscriptionPlayerDTO dto, Inscription inscription, Player player) {
        return InscriptionPlayer.builder()
                .inscription(inscription)
                .player(player)
                .build();
    }
}
