package co.edu.uptc.backend_tc.controller;

import co.edu.uptc.backend_tc.dto.LoginRequestDTO;
import co.edu.uptc.backend_tc.dto.LoginResponseDTO;
import co.edu.uptc.backend_tc.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // 👈 importante para el front
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
