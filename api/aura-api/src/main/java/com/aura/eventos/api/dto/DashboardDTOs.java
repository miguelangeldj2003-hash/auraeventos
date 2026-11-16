package com.aura.eventos.api.dto;

import java.util.List;

public class DashboardDTOs {
    public record MesIngreso(String mes, Double total) {}
    public record TopArticulo(String nombre, Integer cantidad, Integer porcentaje) {}

    public record DashboardDto(
        Integer pendientes, Integer aprobados, Double ingresosMes, Double ingresosTotales,
        Integer variacionMes, Integer ocupacionMes, List<MesIngreso> ingresosMensuales,
        List<TopArticulo> topArticulos, List<ReservacionDTOs.ReservacionDto> recientes,
        List<ReservacionDTOs.ReservacionDto> entregasHoy, ReservacionDTOs.ReservacionDto proxima
    ) {}
}
