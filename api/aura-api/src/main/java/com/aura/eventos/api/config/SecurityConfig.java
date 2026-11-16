package com.aura.eventos.api.config;

import com.aura.eventos.api.model.RolUsuario;
import com.aura.eventos.api.model.Usuario;
import com.aura.eventos.api.repository.UsuarioRepository;
import com.aura.eventos.api.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(HttpMethod.GET, "/api/salud", "/api/categorias", "/api/articulos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                // Endpoints de administradora
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/reservaciones/*/aprobar", "/api/reservaciones/*/rechazar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/evidencias").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${aura.admin.correo:admin@aura.com}") String adminCorreo,
            @Value("${aura.admin.password:Admin123!}") String adminPassword
    ) {
        return args -> {
            if (!usuarioRepository.existsByCorreoIgnoreCase(adminCorreo)) {
                Usuario admin = new Usuario(
                        "Administradora Aura",
                        adminCorreo,
                        passwordEncoder.encode(adminPassword),
                        "55 1234 5678",
                        RolUsuario.ADMIN
                );
                usuarioRepository.save(admin);
            }
        };
    }
}
