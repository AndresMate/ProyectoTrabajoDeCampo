package co.edu.uptc.backend_tc.config;

import co.edu.uptc.backend_tc.entity.User;
import co.edu.uptc.backend_tc.model.UserRole;
import co.edu.uptc.backend_tc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existe un Super Admin
        if (userRepository.findByEmailIgnoreCase("superadmin@uptc.edu.co").isEmpty()) {
            User superAdmin = User.builder()
                    .fullName("Super Administrador")
                    .email("superadmin@uptc.edu.co")
                    .role(UserRole.SUPER_ADMIN)
                    .passwordHash(passwordEncoder.encode("Admin123!")) // Cambia esta contraseña
                    .isActive(true)
                    .forcePasswordChange(false)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();

            userRepository.save(superAdmin);
            System.out.println("✅ Super Admin creado: superadmin@uptc.edu.co");
        } else {
            System.out.println("ℹ️  Super Admin ya existe en el sistema");
        }
    }
}