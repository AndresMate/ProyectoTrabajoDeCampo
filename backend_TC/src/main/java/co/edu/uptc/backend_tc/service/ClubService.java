package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Club;
import co.edu.uptc.backend_tc.repository.ClubRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubService {

    private final ClubRepository repository;

    public ClubService(ClubRepository repository) {
        this.repository = repository;
    }

    public List<Club> getAll() {
        return repository.findAll();
    }

    public Club getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found with id: " + id));
    }

    public Club create(Club club) {
        return repository.save(club);
    }

    public Club update(Long id, Club club) {
        Club existing = getById(id);
        existing.setName(club.getName());
        existing.setDescription(club.getDescription());
        existing.setIsActive(club.getIsActive());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
