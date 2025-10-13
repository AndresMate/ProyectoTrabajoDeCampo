package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.SportDTO;
import co.edu.uptc.backend_tc.dto.response.SportSummaryDTO;
import co.edu.uptc.backend_tc.entity.Sport;
import org.springframework.stereotype.Component;

@Component
public class SportMapper {

    public SportDTO toDTO(Sport entity) {
        if (entity == null) return null;

        return SportDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }

    public Sport toEntity(SportDTO dto) {
        if (dto == null) return null;

        return Sport.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive())
                .build();
    }

    public SportSummaryDTO toSummaryDTO(Sport entity) {
        if (entity == null) return null;

        return SportSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public void updateEntityFromDTO(SportDTO dto, Sport entity) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}