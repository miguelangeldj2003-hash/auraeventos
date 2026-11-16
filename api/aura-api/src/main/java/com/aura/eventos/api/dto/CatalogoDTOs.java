package com.aura.eventos.api.dto;

public class CatalogoDTOs {
    public record CategoriaDto(Long id, String nombre, String seccion) {}

    public record ArticuloDto(
        Long id, Long categoriaId, String categoria, String seccion,
        String nombre, String descripcion, Double precioBase, Integer stockTotal,
        Integer disponibles, String imagenUrl, Boolean activo,
        String sku, String dimensiones, String material, String incluye,
        String pesoMax, Boolean enMantenimiento, Integer rentas
    ) {}

    public record ArticuloRequest(
        Long categoriaId, String nombre, String descripcion, Double precioBase,
        Integer stockTotal, String imagenUrl, String sku, String dimensiones,
        String material, String incluye, String pesoMax, Boolean enMantenimiento
    ) {}
}
