package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long>,
        JpaSpecificationExecutor<Sport> {

    // Búsquedas por nombre
    Optional<Sport> findByName(String name);
    Optional<Sport> findByNameIgnoreCase(String name);

    // Existencia
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);

    // Por estado
    List<Sport> findByIsActiveTrue();
    List<Sport> findByIsActiveFalse();
}