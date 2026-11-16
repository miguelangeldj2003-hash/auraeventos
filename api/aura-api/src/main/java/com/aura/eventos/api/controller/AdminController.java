package com.aura.eventos.api.controller;

import com.aura.eventos.api.dto.CatalogoDTOs.*;
import com.aura.eventos.api.dto.DashboardDTOs.DashboardDto;
import com.aura.eventos.api.service.DashboardService;
import com.aura.eventos.api.service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final InventarioService inventarioService;
    private final DashboardService dashboardService;

    public AdminController(InventarioService inventarioService, DashboardService dashboardService) {
        this.inventarioService = inventarioService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/articulos")
    public ResponseEntity<List<ArticuloDto>> adminArticulos() {
        return ResponseEntity.ok(inventarioService.adminArticulos());
    }

    @PostMapping("/articulos")
    public ResponseEntity<ArticuloDto> crearArticulo(@RequestBody ArticuloRequest req) {
        return ResponseEntity.ok(inventarioService.crearArticulo(req));
    }

    @PutMapping("/articulos/{id}")
    public ResponseEntity<ArticuloDto> actualizarArticulo(@PathVariable("id") Long id, @RequestBody ArticuloRequest req) {
        return ResponseEntity.ok(inventarioService.actualizarArticulo(id, req));
    }

    @PatchMapping("/articulos/{id}/baja")
    public ResponseEntity<ArticuloDto> baja(@PathVariable("id") Long id) {
        return ResponseEntity.ok(inventarioService.cambiarEstado(id, false));
    }

    @PatchMapping("/articulos/{id}/alta")
    public ResponseEntity<ArticuloDto> alta(@PathVariable("id") Long id) {
        return ResponseEntity.ok(inventarioService.cambiarEstado(id, true));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> dashboard() {
        return ResponseEntity.ok(dashboardService.obtenerDashboard());
    }
}
