package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>,
        JpaSpecificationExecutor<Category> {

    // Buscar categorías por deporte
    List<Category> findBySportId(Long sportId);

    // Buscar categorías activas por deporte
    List<Category> findBySportIdAndIsActiveTrue(Long sportId);

    // Validar existencia por nombre y deporte
    boolean existsByNameAndSportId(String name, Long sportId);
    boolean existsByNameIgnoreCaseAndSportId(String name, Long sportId);

    // Listar todas las categorías activas
    List<Category> findByIsActiveTrue();
}
