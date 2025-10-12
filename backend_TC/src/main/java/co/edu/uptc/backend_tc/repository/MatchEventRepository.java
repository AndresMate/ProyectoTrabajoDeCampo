package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchId(Long matchId);
    List<MatchEvent> findByPlayerId(Long playerId);
}
