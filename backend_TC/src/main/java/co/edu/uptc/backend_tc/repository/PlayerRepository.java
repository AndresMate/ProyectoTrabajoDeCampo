package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long>,
        JpaSpecificationExecutor<Player> {

    // Búsquedas únicas
    Optional<Player> findByDocumentNumber(String documentNumber);
    Optional<Player> findByInstitutionalEmail(String email);
    Optional<Player> findByStudentCode(String code);

    // Existencia (para validaciones)
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByInstitutionalEmail(String email);
    boolean existsByStudentCode(String code);

    // Búsquedas por estado
    List<Player> findByIsActiveTrue();
    Page<Player> findByIsActiveTrue(Pageable pageable);

    // Búsquedas con like
    Page<Player> findByFullNameContainingIgnoreCase(String name, Pageable pageable);

    // Conteos
    long countByIsActiveTrue();

    // Query personalizada - Jugadores por equipo
    @Query("SELECT p FROM Player p " +
            "JOIN TeamRoster tr ON tr.player = p " +
            "WHERE tr.team.id = :teamId")
    List<Player> findByTeamId(@Param("teamId") Long teamId);

    // Query personalizada - Jugadores con estadísticas
    @Query("SELECT p FROM Player p " +
            "LEFT JOIN FETCH p.teamRosters " +
            "WHERE p.id = :playerId")
    Optional<Player> findByIdWithTeams(@Param("playerId") Long playerId);
}
