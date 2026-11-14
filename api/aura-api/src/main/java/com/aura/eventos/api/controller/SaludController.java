package com.aura.eventos.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/salud")
public class SaludController {

    @GetMapping
    public ResponseEntity<Map<String, String>> salud() {
        return ResponseEntity.ok(Map.of("status", "UP", "servicio", "Aura Eventos API"));
    }
}
