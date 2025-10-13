package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.TeamRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRosterRepository extends JpaRepository<TeamRoster, Long> {

    // Por equipo
    List<TeamRoster> findByTeamId(Long teamId);

    @Query("SELECT tr FROM TeamRoster tr " +
            "JOIN FETCH tr.player " +
            "WHERE tr.team.id = :teamId")
    List<TeamRoster> findByTeamIdWithPlayer(@Param("teamId") Long teamId);

    // Por jugador
    List<TeamRoster> findByPlayerId(Long playerId);

    // Existencia y validaciones
    boolean existsByTeamIdAndPlayerId(Long teamId, Long playerId);
    boolean existsByPlayerIdAndTeamIsActiveTrue(Long playerId);

    // Conteos
    long countByTeamId(Long teamId);
    long countByPlayerId(Long playerId);

    // Capitanes
    List<TeamRoster> findByTeamIdAndIsCaptainTrue(Long teamId);

    // Buscar por número de camiseta
    boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);

    // Elimina por teamId y playerId
    void deleteByTeamIdAndPlayerId(Long teamId, Long playerId);

    // Busca por teamId y playerId
    Optional<TeamRoster> findByTeamIdAndPlayerId(Long teamId, Long playerId);

    boolean existsByPlayerIdAndTeamId(Long playerId, Long teamId);
}
