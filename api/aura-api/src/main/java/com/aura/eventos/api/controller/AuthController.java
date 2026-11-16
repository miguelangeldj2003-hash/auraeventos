package com.aura.eventos.api.controller;

import com.aura.eventos.api.dto.AuthDTOs.*;
import com.aura.eventos.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@RequestBody RegistroRequest req) {
        return ResponseEntity.ok(authService.registro(req));
    }
}
