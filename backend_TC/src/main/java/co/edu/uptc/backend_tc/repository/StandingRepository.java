package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StandingRepository extends JpaRepository<Standing, Long> {
    List<Standing> findByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);
    Optional<Standing> findByTournamentIdAndCategoryIdAndTeamId(Long tournamentId, Long categoryId, Long teamId);
}
