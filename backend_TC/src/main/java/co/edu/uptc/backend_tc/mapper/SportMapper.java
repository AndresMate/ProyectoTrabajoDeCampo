package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.entity.Sport;

public class SportMapper {

    public static SportDTO toDTO(Sport sport) {
        return SportDTO.builder()
                .id(sport.getId())
                .name(sport.getName())
                .description(sport.getDescription())
                .isActive(sport.getIsActive())
                .build();
    }

    public static Sport toEntity(SportDTO dto) {
        return Sport.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
