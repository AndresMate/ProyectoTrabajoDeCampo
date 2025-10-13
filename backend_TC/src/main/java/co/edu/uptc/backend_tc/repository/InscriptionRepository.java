package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.model.InscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long>,
        JpaSpecificationExecutor<Inscription> {

    // Por torneo
    List<Inscription> findByTournamentId(Long tournamentId);

    // Por torneo y categoría
    List<Inscription> findByTournamentIdAndCategoryId(Long tournamentId, Long categoryId);

    // Por estado
    List<Inscription> findByStatus(InscriptionStatus status);
    List<Inscription> findByTournamentIdAndStatus(Long tournamentId, InscriptionStatus status);

    // Conteos
    long countByTournamentId(Long tournamentId);
    long countByTournamentIdAndStatus(Long tournamentId, InscriptionStatus status);

    // Existencia - Validaciones importantes
    boolean existsByTournamentIdAndDelegateId(Long tournamentId, Long delegateId);

    boolean existsByTournamentIdAndCategoryIdAndDelegateId(
            Long tournamentId,
            Long categoryId,
            Long delegateId
    );

    boolean existsByTournamentIdAndCategoryIdAndTeamNameIgnoreCase(
            Long tournamentId,
            Long categoryId,
            String teamName
    );

    // Por club
    List<Inscription> findByClubId(Long clubId);

    // Query con fetch para evitar N+1
    @Query("SELECT i FROM Inscription i " +
            "LEFT JOIN FETCH i.players " +
            "WHERE i.id = :inscriptionId")
    Inscription findByIdWithPlayers(@Param("inscriptionId") Long inscriptionId);
}
