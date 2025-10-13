package co.edu.uptc.backend_tc.repository;

import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Por email (para login)
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);

    // Existencia
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    // Por rol
    List<User> findByRole(UserRole role);

    // Usuarios activos
    List<User> findByIsActiveTrue();
    List<User> findByIsActiveTrueAndRole(UserRole role);
}