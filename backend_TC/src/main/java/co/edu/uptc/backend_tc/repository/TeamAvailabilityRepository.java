package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.Team;
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
    // 🔹 Buscar las franjas que pertenecen a una inscripción pendiente
    List<TeamAvailability> findByInscription(Inscription inscription);

    // (opcional) por día de la semana y equipo

    // Eliminar todas las disponibilidades de un equipo
    void deleteByTeamId(Long teamId);

    List<TeamAvailability> findByTeamAndAvailableTrue(Team team);

    List<TeamAvailability> findByInscriptionAndAvailableTrue(Inscription inscription);

    boolean existsByTeam(Team team);


    List<TeamAvailability> findByTeamAndDayOfWeek(Team team, DayOfWeek dayOfWeek);
}