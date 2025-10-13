package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.MatchEvent;
import co.edu.uptc.backend_tc.model.MatchEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    // Por partido
    List<MatchEvent> findByMatchId(Long matchId);
    List<MatchEvent> findByMatchIdOrderByMinuteAsc(Long matchId);

    // Por jugador
    List<MatchEvent> findByPlayerId(Long playerId);

    // Por tipo
    List<MatchEvent> findByMatchIdAndType(Long matchId, MatchEventType type);

    // Conteos para estadísticas
    long countByPlayerIdAndType(Long playerId, MatchEventType type);

    @Query("SELECT COUNT(me) FROM MatchEvent me " +
            "WHERE me.player.id = :playerId " +
            "AND me.type IN :types")
    long countByPlayerIdAndTypes(
            @Param("playerId") Long playerId,
            @Param("types") List<MatchEventType> types
    );

    // Goleadores de un torneo
    @Query("SELECT me.player.id, COUNT(me) as goals FROM MatchEvent me " +
            "WHERE me.match.tournament.id = :tournamentId " +
            "AND me.type = 'GOAL' " +
            "GROUP BY me.player.id " +
            "ORDER BY goals DESC")
    List<Object[]> findTopScorersByTournament(@Param("tournamentId") Long tournamentId);
}