package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
