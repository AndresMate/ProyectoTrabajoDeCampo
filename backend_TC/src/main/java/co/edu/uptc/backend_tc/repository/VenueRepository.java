package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // Búsqueda por nombre
    Optional<Venue> findByNameIgnoreCase(String name);

    // Búsqueda con like
    List<Venue> findByNameContainingIgnoreCase(String name);

    // Existencia
    boolean existsByNameIgnoreCase(String name);
}