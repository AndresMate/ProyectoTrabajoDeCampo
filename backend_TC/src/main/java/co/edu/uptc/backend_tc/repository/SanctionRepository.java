package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Sanction;
import co.edu.uptc.backend_tc.model.SanctionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SanctionRepository extends JpaRepository<Sanction, Long> {

    // Por equipo
    List<Sanction> findByTeamId(Long teamId);

    // Por jugador
    List<Sanction> findByPlayerId(Long playerId);

    // Por partido
    List<Sanction> findByMatchId(Long matchId);

    // Por tipo
    List<Sanction> findByType(SanctionType type);

    // Sanciones activas
    @Query("SELECT s FROM Sanction s " +
            "WHERE s.player.id = :playerId " +
            "AND (s.validUntil IS NULL OR s.validUntil > :now)")
    List<Sanction> findActiveByPlayerId(
            @Param("playerId") Long playerId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT s FROM Sanction s " +
            "WHERE s.team.id = :teamId " +
            "AND (s.validUntil IS NULL OR s.validUntil > :now)")
    List<Sanction> findActiveByTeamId(
            @Param("teamId") Long teamId,
            @Param("now") LocalDateTime now
    );

    // Conteos
    long countByPlayerId(Long playerId);
    long countByPlayerIdAndType(Long playerId, SanctionType type);
}