package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
}
