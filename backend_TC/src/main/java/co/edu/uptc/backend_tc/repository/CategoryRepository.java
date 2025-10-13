package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>,
        JpaSpecificationExecutor<Category> {

    // Por deporte
    List<Category> findBySportId(Long sportId);
    List<Category> findBySportIdAndIsActiveTrue(Long sportId);

    // Existencia
    boolean existsByNameAndSportId(String name, Long sportId);
    boolean existsByNameIgnoreCaseAndSportId(String name, Long sportId);

    // Por estado
    List<Category> findByIsActiveTrue();
}