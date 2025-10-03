package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Sport;
import co.edu.uptc.backend_tc.repository.SportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SportService {

    private final SportRepository repository;

    public SportService(SportRepository repository) {
        this.repository = repository;
    }

    public List<Sport> getAll() {
        return repository.findAll();
    }

    public Sport getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found with id: " + id));
    }

    public Sport create(Sport sport) {
        return repository.save(sport);
    }

    public Sport update(Long id, Sport sport) {
        Sport existing = getById(id);
        existing.setName(sport.getName());
        existing.setDescription(sport.getDescription());
        existing.setIsActive(sport.getIsActive());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
