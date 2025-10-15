package co.edu.uptc.backend_tc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http))
                .csrf(csrf -> csrf.disable()) // ✅ Solo para desarrollo
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 ENDPOINTS PÚBLICOS (sin autenticación)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/auth/**",           // Autenticación
                                "/api/tournaments/public/**", // Torneos públicos
                                "/api/matches/public/**",     // Partidos públicos
                                "/api/standings/public/**",   // Tablas públicas
                                "/api/inscriptions/**",       // Inscripciones (usuarios normales)
                                "/api/sports/public/**",      // Deportes públicos
                                "/api/venues/public/**"       // Espacios públicos
                        ).permitAll()

                        // 👑 SUPER_ADMIN - Gestión completa de usuarios
                        .requestMatchers("/api/users/**").hasRole("SUPER_ADMIN")

                        // ⚙️ ADMIN - Gestión de torneos, deportes, espacios + referís
                        .requestMatchers(
                                "/api/tournaments/**",
                                "/api/categories/**",
                                "/api/sports/**",
                                "/api/venues/**",
                                "/api/scenarios/**",
                                "/api/inscriptions/admin/**"  // Aprobación de inscripciones
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 🏅 REFEREE - Gestión de partidos y sanciones
                        .requestMatchers(
                                "/api/matches/**",
                                "/api/match-results/**",
                                "/api/match-events/**",
                                "/api/sanctions/**"
                        ).hasAnyRole("REFEREE", "ADMIN", "SUPER_ADMIN")

                        // 📊 ENDPOINTS DE CONSULTA - Todos los roles autenticados
                        .requestMatchers(
                                "/api/teams/**",
                                "/api/players/**",
                                "/api/clubs/**",
                                "/api/standings/**"
                        ).authenticated()

                        // ❌ Cualquier otra endpoint requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // ✅ Autenticación básica para desarrollo

        return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
