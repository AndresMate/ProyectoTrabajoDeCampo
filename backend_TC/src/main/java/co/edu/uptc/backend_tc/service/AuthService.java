package co.edu.uptc.backend_tc.service;

import co.edu.uptc.backend_tc.dto.LoginRequestDTO;
import co.edu.uptc.backend_tc.dto.LoginResponseDTO;
import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.exception.ResourceNotFoundException;
import co.edu.uptc.backend_tc.exception.UnauthorizedException;
import co.edu.uptc.backend_tc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            log.info("🔐 Login attempt for: {}", request.getEmail());

            // Buscar usuario primero
            User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("❌ User not found: {}", request.getEmail());
                        return new UnauthorizedException("Invalid email or password");
                    });

            log.info("✅ User found: {} | Active: {} | Role: {}",
                    user.getEmail(), user.getIsActive(), user.getRole());

            // Verificar contraseña ANTES de autenticar
            boolean passwordMatches = passwordEncoder.matches(
                    request.getPassword(),
                    user.getPasswordHash()
            );
            log.info("🔑 Password matches: {}", passwordMatches);

            if (!passwordMatches) {
                log.error("❌ Wrong password for: {}", request.getEmail());
                throw new UnauthorizedException("Invalid email or password");
            }

            // Ahora sí, autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            log.info("✅ Authentication successful for: {}", request.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User authenticatedUser = (User) authentication.getPrincipal();

            String token = jwtService.generateToken(authenticatedUser);

            return LoginResponseDTO.builder()
                    .token(token)
                    .email(authenticatedUser.getEmail())
                    .fullName(authenticatedUser.getFullName())
                    .role(authenticatedUser.getRole())
                    .forcePasswordChange(authenticatedUser.getForcePasswordChange())
                    .userId(authenticatedUser.getId())
                    .build();

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Authentication error: {}", e.getMessage(), e);
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    public void forcePasswordChange(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        userRepository.save(user);

        log.info("✅ Password changed for user: {}", user.getEmail());
    }
}