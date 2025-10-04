package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    List<Inscription> findByTournamentId(Long tournamentId);
}
