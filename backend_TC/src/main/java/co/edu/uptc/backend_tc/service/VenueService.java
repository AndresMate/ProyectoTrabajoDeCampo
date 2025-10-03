package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository repository;

    public VenueService(VenueRepository repository) {
        this.repository = repository;
    }

    public List<Venue> getAll() {
        return repository.findAll();
    }

    public Venue getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with id: " + id));
    }

    public Venue create(Venue venue) {
        return repository.save(venue);
    }

    public Venue update(Long id, Venue venue) {
        Venue existing = getById(id);
        existing.setName(venue.getName());
        existing.setAddress(venue.getAddress());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
