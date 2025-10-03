package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SportRepository extends JpaRepository<Sport, Long> {
}
