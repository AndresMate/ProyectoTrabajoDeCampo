package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandingRepository extends JpaRepository<Standing, Long> {

    // Por torneo y categoría (con fetch para evitar N+1)
    @Query("SELECT s FROM Standing s " +
            "JOIN FETCH s.team t " +
            "JOIN FETCH s.tournament " +
            "JOIN FETCH s.category " +
            "WHERE s.tournament.id = :tournamentId " +
            "AND s.category.id = :categoryId " +
            "ORDER BY s.points DESC, s.goalsFor DESC")
    List<Standing> findByTournamentIdAndCategoryId(
            @Param("tournamentId") Long tournamentId,
            @Param("categoryId") Long categoryId
    );

    // Buscar standing específico
    @Query("SELECT s FROM Standing s " +
            "WHERE s.tournament.id = :tournamentId " +
            "AND s.category.id = :categoryId " +
            "AND s.team.id = :teamId")
    Optional<Standing> findByTournamentIdAndCategoryIdAndTeamId(
            @Param("tournamentId") Long tournamentId,
            @Param("categoryId") Long categoryId,
            @Param("teamId") Long teamId
    );

    // Eliminar por torneo y categoría (para recalcular)
    void deleteByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);
}
