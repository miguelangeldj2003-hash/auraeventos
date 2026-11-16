package com.aura.eventos.api.dto;

import java.util.List;

public class ReservacionDTOs {
    public record ItemRequest(Long articuloId, Integer cantidad) {}

    public record ReservacionRequest(
        String fechaEvento, String fechaFin, String direccion, String entorno,
        String notas, Double latitud, Double longitud, List<ItemRequest> items
    ) {}

    public record DetalleDto(
        Long articuloId, String sku, String nombre, Integer cantidad,
        Double precioUnitario, Double subtotal
    ) {}

    public record ReservacionDto(
        Long id, String folio, Long clienteId, String cliente, String correo,
        String telefono, String fechaEvento, String fechaFin, Integer dias,
        String direccion, String entorno, String notas,
        Double latitud, Double longitud, String estado, String motivoRechazo,
        String creadoEn, Double costoFlete, Double subtotal, Double iva,
        Double total, List<DetalleDto> detalles
    ) {}

    public record AprobacionRequest(Double costoFlete) {}
    public record RechazoRequest(String motivo) {}
}
