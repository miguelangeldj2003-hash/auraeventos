package com.aura.eventos.api.security;

import com.aura.eventos.api.model.RolUsuario;

public record UsuarioAutenticado(
    Long id,
    String correo,
    RolUsuario rol
) {}
