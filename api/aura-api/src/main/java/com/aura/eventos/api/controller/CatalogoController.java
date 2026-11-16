package com.aura.eventos.api.controller;

import com.aura.eventos.api.dto.CatalogoDTOs.*;
import com.aura.eventos.api.service.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaDto>> categorias() {
        return ResponseEntity.ok(catalogoService.categorias());
    }

    @GetMapping("/articulos")
    public ResponseEntity<List<ArticuloDto>> articulos(
            @RequestParam(value = "desde", required = false) String desde,
            @RequestParam(value = "hasta", required = false) String hasta
    ) {
        return ResponseEntity.ok(catalogoService.articulos(desde, hasta));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<String>> disponibilidad(
            @RequestParam("items") String items,
            @RequestParam("mes") String mes
    ) {
        return ResponseEntity.ok(catalogoService.disponibilidad(items, mes));
    }
}
