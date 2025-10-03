package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
