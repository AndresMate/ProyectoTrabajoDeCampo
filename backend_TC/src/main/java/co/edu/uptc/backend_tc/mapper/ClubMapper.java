package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.entity.Club;

public class ClubMapper {

    public static ClubDTO toDTO(Club club) {
        return ClubDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .isActive(club.getIsActive())
                .build();
    }

    public static Club toEntity(ClubDTO dto) {
        return Club.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive())
                .build();
    }
}
