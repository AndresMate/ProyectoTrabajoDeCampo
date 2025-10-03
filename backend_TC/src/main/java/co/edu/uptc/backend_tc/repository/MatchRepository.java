package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
