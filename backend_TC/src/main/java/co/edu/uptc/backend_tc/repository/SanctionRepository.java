package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Sanction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SanctionRepository extends JpaRepository<Sanction, Long> {
    List<Sanction> findByTeamId(Long teamId);
    List<Sanction> findByPlayerId(Long playerId);
    List<Sanction> findByMatchId(Long matchId);


}
