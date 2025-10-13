package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>,
        JpaSpecificationExecutor<Team> {

    // Por torneo
    List<Team> findByTournamentId(Long tournamentId);

    // Por torneo y categoría
    @Query("SELECT t FROM Team t " +
            "WHERE t.tournament.id = :tournamentId " +
            "AND t.category.id = :categoryId")
    List<Team> findByTournamentIdAndCategoryId(
            @Param("tournamentId") Long tournamentId,
            @Param("categoryId") Long categoryId
    );

    // Por club
    List<Team> findByClubId(Long clubId);
    List<Team> findByClubIdAndIsActiveTrue(Long clubId);

    // Conteos
    long countByTournamentId(Long tournamentId);
    long countByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);

    // Existencia
    boolean existsByClubIdAndIsActiveTrue(Long clubId);
    boolean existsByTournamentIdAndNameIgnoreCase(Long tournamentId, String name);

    // Por estado
    List<Team> findByIsActiveTrue();

    // Con roster
    @Query("SELECT t FROM Team t " +
            "LEFT JOIN FETCH t.roster " +
            "WHERE t.id = :teamId")
    Optional<Team> findByIdWithRoster(@Param("teamId") Long teamId);
}