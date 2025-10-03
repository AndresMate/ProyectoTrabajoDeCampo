package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
}
