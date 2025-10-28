package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Match;
import co.edu.uptc.backend_tc.model.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>,
        JpaSpecificationExecutor<Match> {

    // Por torneo
    List<Match> findByTournamentId(Long tournamentId);

    // Por torneo y categoría
    List<Match> findByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);

    // Por equipo (local o visitante)
    @Query("SELECT m FROM Match m " +
            "WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId")
    List<Match> findByTeamId(@Param("teamId") Long teamId);

    // Por estado
    List<Match> findByStatus(MatchStatus status);
    Page<Match> findByStatus(MatchStatus status, Pageable pageable);

    // Por fechas
    List<Match> findByStartsAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM Match m " +
            "WHERE m.startsAt >= :start " +
            "AND m.startsAt <= :end " +
            "AND m.status = :status")
    List<Match> findByDateRangeAndStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") MatchStatus status
    );

    // Por escenario
    List<Match> findByScenarioId(Long scenarioId);

    // Por árbitro
    List<Match> findByRefereeId(Long refereeId);

    // Conteos
    long countByTournamentId(Long tournamentId);
    long countByTournamentIdAndStatus(Long tournamentId, MatchStatus status);
    long countByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);
    long countByTournamentIdAndStatusNot(Long tournamentId, MatchStatus status);

    // Próximos partidos
    @Query("SELECT m FROM Match m " +
            "WHERE m.status = 'SCHEDULED' " +
            "AND m.startsAt > CURRENT_TIMESTAMP " +
            "ORDER BY m.startsAt ASC")
    List<Match> findUpcomingMatches(Pageable pageable);

    // Conteo de partidos jugados por equipo (excluyendo estado SCHEDULED)
    @Query("SELECT COUNT(m) FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) AND m.status <> :status")
    long countByTeamIdAndStatusNot(@Param("teamId") Long teamId, @Param("status") MatchStatus status);

    // Conteo total de partidos por equipo
    @Query("SELECT COUNT(m) FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId")
    long countByTeamId(@Param("teamId") Long teamId);

    long countByScenarioId(Long scenarioId);

    @Query("""
        SELECT m FROM Match m
        LEFT JOIN FETCH m.tournament
        LEFT JOIN FETCH m.category
        LEFT JOIN FETCH m.scenario
        LEFT JOIN FETCH m.homeTeam
        LEFT JOIN FETCH m.awayTeam
        LEFT JOIN FETCH m.referee
        LEFT JOIN FETCH m.result
    """)
    List<Match> findAllWithRelations();
}
