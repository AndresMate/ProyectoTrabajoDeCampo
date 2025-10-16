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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/auth/**",
                                "/api/tournaments/public/**",
                                "/api/matches/public/**",
                                "/api/standings/public/**",
                                "/api/inscriptions/**",
                                "/api/sports/public/**",
                                "/api/venues/public/**"
                        ).permitAll()
                        .requestMatchers("/api/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(
                                "/api/tournaments/**",
                                "/api/categories/**",
                                "/api/sports/**",
                                "/api/venues/**",
                                "/api/scenarios/**",
                                "/api/inscriptions/admin/**"
                        ).hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(
                                "/api/matches/**",
                                "/api/match-results/**",
                                "/api/match-events/**",
                                "/api/sanctions/**"
                        ).hasAnyRole("REFEREE", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers(
                                "/api/teams/**",
                                "/api/players/**",
                                "/api/clubs/**",
                                "/api/standings/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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