package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.CategoryDTO;
import co.edu.uptc.backend_tc.entity.Category;
import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.mapper.CategoryMapper;
import co.edu.uptc.backend_tc.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryDTO> getAll() {
        return service.getAll()
                .stream()
                .map(CategoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CategoryDTO getById(@PathVariable Long id) {
        return CategoryMapper.toDTO(service.getById(id));
    }

    @PostMapping
    public CategoryDTO create(@RequestBody CategoryDTO dto) {
        Sport sport = service.getSportById(dto.getSportId());
        Category category = CategoryMapper.toEntity(dto, sport);
        return CategoryMapper.toDTO(service.create(category));
    }

    @PutMapping("/{id}")
    public CategoryDTO update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        Sport sport = service.getSportById(dto.getSportId());
        Category category = CategoryMapper.toEntity(dto, sport);
        return CategoryMapper.toDTO(service.update(id, category));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
