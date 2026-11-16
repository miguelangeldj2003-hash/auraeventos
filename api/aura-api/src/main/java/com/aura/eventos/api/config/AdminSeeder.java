package com.aura.eventos.api.config;

import com.aura.eventos.api.model.RolUsuario;
import com.aura.eventos.api.model.Usuario;
import com.aura.eventos.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminCorreo;
    private final String adminPassword;

    public AdminSeeder(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${aura.admin.correo:}") String adminCorreo,
            @Value("${aura.admin.password:}") String adminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminCorreo = adminCorreo;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD no está definida en las variables de entorno / .env");
        }

        String correo = (adminCorreo != null && !adminCorreo.isBlank()) ? adminCorreo.trim().toLowerCase() : "admin@aura.com";

        if (!usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            Usuario admin = new Usuario(
                    "Administradora Aura",
                    correo,
                    passwordEncoder.encode(adminPassword),
                    "55 1234 5678",
                    RolUsuario.ADMIN
            );
            usuarioRepository.save(admin);
        }
    }
}
