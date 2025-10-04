package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.TeamAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamAvailabilityRepository extends JpaRepository<TeamAvailability, Long> {
    List<TeamAvailability> findByTeamId(Long teamId);
}
