package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.TeamAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TeamAvailabilityRepository extends JpaRepository<TeamAvailability, Long> {

    // Por equipo
    List<TeamAvailability> findByTeamId(Long teamId);
    List<TeamAvailability> findByTeamIdAndAvailableTrue(Long teamId);

    // Por día
    List<TeamAvailability> findByTeamIdAndDayOfWeek(Long teamId, DayOfWeek dayOfWeek);

    // Eliminar todas las disponibilidades de un equipo
    void deleteByTeamId(Long teamId);
}