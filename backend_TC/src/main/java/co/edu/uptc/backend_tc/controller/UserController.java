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

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return UserMapper.toDTO(userService.getUserById(id));
    }

    @PostMapping
    public UserDTO createUser(@RequestBody User user) {
        return UserMapper.toDTO(userService.createUser(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
