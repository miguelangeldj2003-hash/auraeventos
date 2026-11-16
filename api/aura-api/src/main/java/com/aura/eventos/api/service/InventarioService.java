package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.CatalogoDTOs.*;
import com.aura.eventos.api.exception.ApiException;
import com.aura.eventos.api.model.Articulo;
import com.aura.eventos.api.model.Categoria;
import com.aura.eventos.api.repository.ArticuloRepository;
import com.aura.eventos.api.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventarioService {

    private final ArticuloRepository articuloRepository;
    private final CategoriaRepository categoriaRepository;
    private final CatalogoService catalogoService;

    public InventarioService(ArticuloRepository articuloRepository, CategoriaRepository categoriaRepository, CatalogoService catalogoService) {
        this.articuloRepository = articuloRepository;
        this.categoriaRepository = categoriaRepository;
        this.catalogoService = catalogoService;
    }

    public List<ArticuloDto> adminArticulos() {
        LocalDate hoy = LocalDate.now();
        return articuloRepository.findAll().stream()
                .map(a -> catalogoService.toDto(a, catalogoService.calcularLibres(a, hoy, hoy, -1L)))
                .toList();
    }

    public ArticuloDto crearArticulo(ArticuloRequest req) {
        validarRequest(req);
        Categoria c = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        Articulo a = new Articulo();
        copiarDatos(a, req, c);
        a.setActivo(true);

        Articulo guardado = articuloRepository.save(a);
        return catalogoService.toDto(guardado, guardado.getStockTotal());
    }

    public ArticuloDto actualizarArticulo(Long id, ArticuloRequest req) {
        validarRequest(req);
        Articulo a = articuloRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));

        Categoria c = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        copiarDatos(a, req, c);
        Articulo guardado = articuloRepository.save(a);
        return catalogoService.toDto(guardado, guardado.getStockTotal());
    }

    public ArticuloDto cambiarEstado(Long id, boolean activo) {
        Articulo a = articuloRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));
        a.setActivo(activo);
        Articulo guardado = articuloRepository.save(a);
        return catalogoService.toDto(guardado, guardado.getStockTotal());
    }

    private void validarRequest(ArticuloRequest req) {
        if (req.nombre() == null || req.nombre().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe el nombre del artículo");
        }
        if (req.categoriaId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Elige una categoría");
        }
        if (req.precioBase() == null || req.precioBase() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe una tarifa válida");
        }
        if (req.stockTotal() == null || req.stockTotal() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe el stock total");
        }
    }

    private void copiarDatos(Articulo a, ArticuloRequest r, Categoria c) {
        a.setCategoria(c);
        a.setNombre(r.nombre().trim());
        a.setDescripcion(r.descripcion() != null && !r.descripcion().isBlank() ? r.descripcion().trim() : null);
        a.setPrecioBase(r.precioBase());
        a.setStockTotal(r.stockTotal());
        if (r.imagenUrl() != null && !r.imagenUrl().isBlank()) {
            a.setImagenUrl(r.imagenUrl().trim());
        }
        a.setSku(r.sku() != null && !r.sku().isBlank() ? r.sku().trim() : null);
        a.setDimensiones(r.dimensiones() != null && !r.dimensiones().isBlank() ? r.dimensiones().trim() : null);
        a.setMaterial(r.material() != null && !r.material().isBlank() ? r.material().trim() : null);
        a.setIncluye(r.incluye() != null && !r.incluye().isBlank() ? r.incluye().trim() : null);
        a.setPesoMax(r.pesoMax() != null && !r.pesoMax().isBlank() ? r.pesoMax().trim() : null);
        a.setEnMantenimiento(r.enMantenimiento() != null ? r.enMantenimiento() : false);
    }
}
