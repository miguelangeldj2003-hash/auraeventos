package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.AuthDTOs.*;
import com.aura.eventos.api.exception.ApiException;
import com.aura.eventos.api.model.RolUsuario;
import com.aura.eventos.api.model.Usuario;
import com.aura.eventos.api.repository.UsuarioRepository;
import com.aura.eventos.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest req) {
        if (req.correo() == null || req.password() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Correo y contraseña son requeridos");
        }

        Usuario u = usuarioRepository.findByCorreoIgnoreCase(req.correo().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generarToken(u);
        UsuarioDto dto = new UsuarioDto(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(), u.getRol().name());
        return new AuthResponse(token, dto);
    }

    public AuthResponse registro(RegistroRequest req) {
        if (req.correo() == null || req.password() == null || req.nombre() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nombre, correo y contraseña son requeridos");
        }

        String correoTrim = req.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoTrim)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con este correo");
        }

        String passEncoded = passwordEncoder.encode(req.password());
        Usuario nuevo = new Usuario(req.nombre().trim(), correoTrim, passEncoded, req.telefono(), RolUsuario.CLIENTE);
        Usuario guardado = usuarioRepository.save(nuevo);

        String token = jwtService.generarToken(guardado);
        UsuarioDto dto = new UsuarioDto(guardado.getId(), guardado.getNombre(), guardado.getCorreo(), guardado.getTelefono(), guardado.getRol().name());
        return new AuthResponse(token, dto);
    }
}
