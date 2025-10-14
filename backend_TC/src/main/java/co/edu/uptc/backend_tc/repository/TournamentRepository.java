package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Tournament;
import co.edu.uptc.backend_tc.model.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>,
        JpaSpecificationExecutor<Tournament> {

    // ✅ MÉTODO NUEVO: Buscar por múltiples estados
    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);

    // ✅ MÉTODO ALTERNATIVO: Query personalizada para torneos activos
    @Query("SELECT t FROM Tournament t WHERE t.status IN :statuses")
    List<Tournament> findByStatusList(@Param("statuses") List<TournamentStatus> statuses);

    // ✅ MÉTODO ESPECÍFICO: Torneos activos (abiertos o en progreso)
    @Query("SELECT t FROM Tournament t WHERE t.status = 'OPEN_FOR_INSCRIPTION' OR t.status = 'IN_PROGRESS'")
    List<Tournament> findActiveTournaments();

    // ✅ MÉTODO CON PAGINACIÓN: Torneos activos paginados
    @Query("SELECT t FROM Tournament t WHERE t.status IN ('OPEN_FOR_INSCRIPTION', 'IN_PROGRESS')")
    Page<Tournament> findActiveTournaments(Pageable pageable);

    // Otros métodos existentes...
    List<Tournament> findBySportId(Long sportId);
    List<Tournament> findByStatus(TournamentStatus status);
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);
    List<Tournament> findByCreatedById(Long userId);
    List<Tournament> findByStartDateBetween(LocalDate start, LocalDate end);
    Page<Tournament> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
}