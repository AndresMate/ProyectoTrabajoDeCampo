package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Standing;
import co.edu.uptc.backend_tc.entity.id.StandingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StandingRepository extends JpaRepository<Standing, StandingId> {

    @Query("SELECT s FROM Standing s " +
            "JOIN FETCH s.team " +
            "JOIN FETCH s.tournament " +
            "JOIN FETCH s.category " +
            "WHERE s.tournament.id = :tournamentId AND s.category.id = :categoryId")
    List<Standing> findByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);

    @Query("SELECT s FROM Standing s " +
            "JOIN FETCH s.team " +
            "JOIN FETCH s.tournament " +
            "JOIN FETCH s.category " +
            "WHERE s.tournament.id = :tournamentId AND s.category.id = :categoryId AND s.team.id = :teamId")
    Optional<Standing> findByTournamentIdAndCategoryIdAndTeamId(Long tournamentId, Long categoryId, Long teamId);
}
