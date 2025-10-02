package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.UserDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.mapper.UserMapper;
import co.edu.uptc.backend_tc.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 🔹 GET: Listar todos los usuarios
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 🔹 GET: Obtener usuario por ID
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return UserMapper.toDTO(userService.getUserById(id));
    }

    // 🔹 POST: Crear usuario
    @PostMapping
    public UserDTO createUser(@RequestBody UserDTO dto) {
        User user = UserMapper.toEntity(dto);
        return UserMapper.toDTO(userService.createUser(user));
    }

    // 🔹 PUT: Actualizar usuario
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        User user = UserMapper.toEntity(dto);
        return UserMapper.toDTO(userService.updateUser(id, user));
    }

    // 🔹 DELETE: Eliminar usuario
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
