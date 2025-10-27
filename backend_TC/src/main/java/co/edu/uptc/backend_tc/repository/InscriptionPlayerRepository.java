package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Inscription;
import co.edu.uptc.backend_tc.entity.InscriptionPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionPlayerRepository extends JpaRepository<InscriptionPlayer, Long> {

    // Por inscripción
    List<InscriptionPlayer> findByInscriptionId(Long inscriptionId);

    // Por jugador
    List<InscriptionPlayer> findByPlayerId(Long playerId);

    // Conteos
    long countByInscriptionId(Long inscriptionId);

    // Existencia
    boolean existsByInscriptionIdAndPlayerId(Long inscriptionId, Long playerId);

    // Eliminar todos de una inscripción
    void deleteByInscriptionId(Long inscriptionId);

    void deleteByInscriptionIdAndPlayerId(Long inscriptionId, Long playerId);

    List<InscriptionPlayer> findByInscription(Inscription inscription);
}