package com.aura.eventos.api.dto;

import com.aura.eventos.api.model.RolUsuario;

public class AuthDTOs {
    public record LoginRequest(String correo, String password) {}
    public record RegistroRequest(String nombre, String correo, String password, String telefono) {}
    public record UsuarioDto(Long id, String nombre, String correo, String telefono, String rol) {}
    public record AuthResponse(String token, UsuarioDto usuario) {}
}
