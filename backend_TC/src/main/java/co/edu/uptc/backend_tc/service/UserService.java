package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // 🔹 Listar todos los usuarios
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    // 🔹 Buscar usuario por ID
    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 🔹 Crear usuario
    public User createUser(User user) {
        return repository.save(user);
    }

    // 🔹 Actualizar usuario
    public User updateUser(Long id, User user) {
        User existing = getUserById(id);
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setIsActive(user.getIsActive());
        // ⚠️ Nota: si manejas passwordHash, deberías agregar lógica separada aquí
        return repository.save(existing);
    }

    // 🔹 Eliminar usuario
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
