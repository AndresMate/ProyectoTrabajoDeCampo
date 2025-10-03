package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.repository.CategoryRepository;
import co.edu.uptc.backend_tc.repository.SportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SportRepository sportRepository;

    public CategoryService(CategoryRepository categoryRepository, SportRepository sportRepository) {
        this.categoryRepository = categoryRepository;
        this.sportRepository = sportRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category category) {
        Category existing = getById(id);
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setIsActive(category.getIsActive());
        existing.setSport(category.getSport());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public Sport getSportById(Long id) {
        return sportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found with id: " + id));
    }
}
