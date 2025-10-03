package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
