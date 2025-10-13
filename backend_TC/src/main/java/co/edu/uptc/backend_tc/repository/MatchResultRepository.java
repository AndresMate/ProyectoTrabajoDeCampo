package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    // Por torneo (necesita join)
    @Query("SELECT mr FROM MatchResult mr " +
            "WHERE mr.match.tournament.id = :tournamentId")
    List<MatchResult> findByTournamentId(@Param("tournamentId") Long tournamentId);

    // Por categoría
    @Query("SELECT mr FROM MatchResult mr " +
            "WHERE mr.match.tournament.id = :tournamentId " +
            "AND mr.match.category.id = :categoryId")
    List<MatchResult> findByTournamentIdAndCategoryId(
            @Param("tournamentId") Long tournamentId,
            @Param("categoryId") Long categoryId
    );
}