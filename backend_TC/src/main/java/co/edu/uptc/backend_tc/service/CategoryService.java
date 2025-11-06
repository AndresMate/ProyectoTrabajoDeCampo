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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    
    @PersistenceContext
    private EntityManager entityManager;

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

        // Resetear la secuencia si está desincronizada
        // Usar EntityManager para ejecutar la consulta nativa de forma segura
        try {
            entityManager.createNativeQuery(
                "SELECT setval('categories_id_seq', COALESCE((SELECT MAX(id) FROM categories), 0) + 1, false)"
            ).getSingleResult();
        } catch (Exception e) {
            // Si falla el reset de secuencia, continuar de todas formas
            // (puede que la secuencia no exista o tenga otro nombre)
            // El error se registrará pero no bloqueará la creación
        }

        // Crear la categoría con el nuevo campo
        // Asegurar que no se establezca un ID (debe ser generado automáticamente)
        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .membersPerTeam(dto.getMembersPerTeam())
                .sport(sport)
                .isActive(true)
                .build();
        
        // Asegurar explícitamente que el ID sea null para que se genere automáticamente
        category.setId(null);

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
        // Excluir la categoría actual de la validación
        if (dto.getName() != null &&
                !category.getName().equalsIgnoreCase(dto.getName())) {
            Long sportIdToCheck = dto.getSportId() != null ? dto.getSportId() : category.getSport().getId();
            // Verificar si existe otra categoría (diferente a la actual) con el mismo nombre
            boolean existsOther = categoryRepository.findBySportId(sportIdToCheck).stream()
                    .anyMatch(c -> !c.getId().equals(id) && 
                            c.getName().equalsIgnoreCase(dto.getName()));
            if (existsOther) {
                throw new ConflictException(
                        "Category with this name already exists for this sport",
                        "name",
                        dto.getName()
                );
            }
        }

        // Actualizar la entidad con los datos nuevos (incluye membersPerTeam)
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
