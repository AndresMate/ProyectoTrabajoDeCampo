package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    // Por venue
    List<Scenario> findByVenueId(Long venueId);

    // Escenarios con capacidad mínima
    List<Scenario> findByCapacityGreaterThanEqual(Integer minCapacity);

    // Escenarios para juegos nocturnos
    List<Scenario> findBySupportsNightGamesTrue();


    long countByVenueId(Long venueId);
}