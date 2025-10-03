package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.Scenario;
import co.edu.uptc.backend_tc.entity.Venue;
import co.edu.uptc.backend_tc.repository.ScenarioRepository;
import co.edu.uptc.backend_tc.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final VenueRepository venueRepository;

    public ScenarioService(ScenarioRepository scenarioRepository, VenueRepository venueRepository) {
        this.scenarioRepository = scenarioRepository;
        this.venueRepository = venueRepository;
    }

    public List<Scenario> getAll() {
        return scenarioRepository.findAll();
    }

    public Scenario getById(Long id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found with id: " + id));
    }

    public Scenario create(Scenario scenario) {
        return scenarioRepository.save(scenario);
    }

    public Scenario update(Long id, Scenario scenario) {
        Scenario existing = getById(id);
        existing.setName(scenario.getName());
        existing.setCapacity(scenario.getCapacity());
        existing.setSupportsNightGames(scenario.getSupportsNightGames());
        existing.setVenue(scenario.getVenue());
        return scenarioRepository.save(existing);
    }

    public void delete(Long id) {
        scenarioRepository.deleteById(id);
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with id: " + id));
    }
}
