package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
