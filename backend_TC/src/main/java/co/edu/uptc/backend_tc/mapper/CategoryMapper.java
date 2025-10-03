package co.edu.uptc.backend_tc.mapper;

import co.edu.uptc.backend_tc.dto.CategoryDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Sport;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .sportId(category.getSport().getId())
                .build();
    }

    public static Category toEntity(CategoryDTO dto, Sport sport) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .sport(sport)
                .build();
    }
}
