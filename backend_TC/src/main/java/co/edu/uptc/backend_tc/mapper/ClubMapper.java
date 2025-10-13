package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.ClubDTO;
import co.edu.uptc.backend_tc.dto.response.ClubSummaryDTO;
import co.edu.uptc.backend_tc.entity.Club;
import org.springframework.stereotype.Component;

@Component
public class ClubMapper {

    public ClubDTO toDTO(Club entity) {
        if (entity == null) return null;

        return ClubDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }

    public Club toEntity(ClubDTO dto) {
        if (dto == null) return null;

        return Club.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive())
                .build();
    }

    public ClubSummaryDTO toSummaryDTO(Club entity) {
        if (entity == null) return null;

        return ClubSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}