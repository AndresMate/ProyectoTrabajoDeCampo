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

    // Por deporte
    List<Tournament> findBySportId(Long sportId);
    Page<Tournament> findBySportId(Long sportId, Pageable pageable);

    // Por estado
    List<Tournament> findByStatus(TournamentStatus status);
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    // Por creador
    List<Tournament> findByCreatedById(Long userId);

    // Por fechas
    List<Tournament> findByStartDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT t FROM Tournament t " +
            "WHERE t.status = :status " +
            "AND t.startDate <= :date " +
            "AND t.endDate >= :date")
    List<Tournament> findActiveOnDate(
            @Param("status") TournamentStatus status,
            @Param("date") LocalDate date
    );

    // Búsqueda por nombre
    Page<Tournament> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Existencia
    boolean existsByNameIgnoreCase(String name);
}