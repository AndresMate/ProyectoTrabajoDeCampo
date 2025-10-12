package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.tournament.id = :tournamentId AND t.category.id = :categoryId")
    List<Team> findByTournamentIdAndCategoryId(
            @Param("tournamentId") Long tournamentId,
            @Param("categoryId") Long categoryId
    );
}
