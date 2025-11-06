package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    // Resetear la secuencia de la tabla categories
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "SELECT setval('categories_id_seq', COALESCE((SELECT MAX(id) FROM categories), 0) + 1, false)", nativeQuery = true)
    void resetSequence();
}
