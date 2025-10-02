package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.PlayerDTO;
import co.edu.uptc.backend_tc.entity.Player;

public class PlayerMapper {

    public static PlayerDTO toDTO(Player player) {
        return PlayerDTO.builder()
                .id(player.getId())
                .fullName(player.getFullName())
                .studentCode(player.getStudentCode())
                .documentNumber(player.getDocumentNumber())
                .institutionalEmail(player.getInstitutionalEmail())
                .birthDate(player.getBirthDate())
                .isActive(player.getIsActive())
                .build();
    }

    public static Player toEntity(PlayerDTO dto) {
        return Player.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .studentCode(dto.getStudentCode())
                .documentNumber(dto.getDocumentNumber())
                .institutionalEmail(dto.getInstitutionalEmail())
                .birthDate(dto.getBirthDate())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
