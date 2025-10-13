package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.CategoryDTO;
import co.edu.uptc.backend_tc.dto.page.PageResponseDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.exception.ConflictException;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.mapper.CategoryMapper;
import co.edu.uptc.backend_tc.mapper.MapperUtils;
import co.edu.uptc.backend_tc.repository.CategoryRepository;
import co.edu.uptc.backend_tc.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SportRepository sportRepository;
    private final CategoryMapper categoryMapper;
    private final MapperUtils mapperUtils;

    public PageResponseDTO<CategoryDTO> getAll(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        return mapperUtils.mapPage(page, categoryMapper::toDTO);
    }

    public List<CategoryDTO> getActiveBySport(Long sportId) {
        return mapperUtils.mapList(
                categoryRepository.findBySportIdAndIsActiveTrue(sportId),
                categoryMapper::toDTO
        );
    }

    public CategoryDTO getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryDTO create(CategoryDTO dto) {
        // Verificar deporte
        Sport sport = sportRepository.findById(dto.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));

        // Verificar nombre único en el deporte
        if (categoryRepository.existsByNameIgnoreCaseAndSportId(dto.getName(), sport.getId())) {
            throw new ConflictException(
                    "Category with this name already exists for this sport",
                    "name",
                    dto.getName()
            );
        }

        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .sport(sport)
                .isActive(true)
                .build();

        category = categoryRepository.save(category);
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Verificar deporte si cambió
        Sport sport = null;
        if (dto.getSportId() != null && !dto.getSportId().equals(category.getSport().getId())) {
            sport = sportRepository.findById(dto.getSportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", dto.getSportId()));
        }

        // Validar nombre único si cambió
        if (!category.getName().equalsIgnoreCase(dto.getName()) &&
                categoryRepository.existsByNameIgnoreCaseAndSportId(dto.getName(), category.getSport().getId())) {
            throw new ConflictException(
                    "Category with this name already exists for this sport",
                    "name",
                    dto.getName()
            );
        }

        categoryMapper.updateEntityFromDTO(dto, category, sport);
        category = categoryRepository.save(category);
        return categoryMapper.toDTO(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft delete
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}