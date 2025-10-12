package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);


}
