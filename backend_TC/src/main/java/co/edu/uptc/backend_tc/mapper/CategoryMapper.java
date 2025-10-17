package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.CategoryDTO;
import co.edu.uptc.backend_tc.dto.response.CategorySummaryDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Sport;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    private final SportMapper sportMapper;

    public CategoryMapper(SportMapper sportMapper) {
        this.sportMapper = sportMapper;
    }

    public CategoryDTO toDTO(Category entity) {
        if (entity == null) return null;

        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .membersPerTeam(entity.getMembersPerTeam())
                .isActive(entity.getIsActive())
                .sportId(entity.getSport() != null ? entity.getSport().getId() : null)
                .build();
    }

    public CategorySummaryDTO toSummaryDTO(Category entity) {
        if (entity == null) return null;

        return CategorySummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public void updateEntityFromDTO(CategoryDTO dto, Category entity, Sport sport) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getMembersPerTeam() != null) {
            entity.setMembersPerTeam(dto.getMembersPerTeam());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (sport != null) {
            entity.setSport(sport);
        }
    }
}
