package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.LoginRequestDTO;
import co.edu.uptc.backend_tc.dto.LoginResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.exception.UnauthorizedException;
import co.edu.uptc.backend_tc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Obtener usuario autenticado
            User user = (User) authentication.getPrincipal();

            // Generar token JWT
            String token = jwtService.generateToken(user);

            return LoginResponseDTO.builder()
                    .token(token)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .forcePasswordChange(user.getForcePasswordChange()) // ✅
                    .userId(user.getId())
                    .build();

        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    public void forcePasswordChange(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        userRepository.save(user);
    }
}