package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>,
        JpaSpecificationExecutor<Club> {

    // Búsquedas por nombre
    Optional<Club> findByName(String name);
    Optional<Club> findByNameIgnoreCase(String name);

    // Existencia
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);

    // Por estado
    List<Club> findByIsActiveTrue();
    List<Club> findByIsActiveFalse();
}