package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.DashboardDTOs.*;
import com.aura.eventos.api.model.EstadoReservacion;
import com.aura.eventos.api.model.Reservacion;
import com.aura.eventos.api.repository.ReservacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class DashboardService {

    private final ReservacionRepository reservacionRepository;
    private final ReservacionService reservacionService;

    public DashboardService(ReservacionRepository reservacionRepository, ReservacionService reservacionService) {
        this.reservacionRepository = reservacionRepository;
        this.reservacionService = reservacionService;
    }

    public DashboardDto obtenerDashboard() {
        List<Reservacion> todas = reservacionRepository.findAll();
        List<Reservacion> aprobadas = todas.stream().filter(r -> r.getEstado() == EstadoReservacion.APROBADO).toList();

        LocalDate hoy = LocalDate.now();
        YearMonth actual = YearMonth.from(hoy);

        double ingresosMes = aprobadas.stream()
                .filter(r -> YearMonth.from(r.getFechaEvento()).equals(actual))
                .mapToDouble(Reservacion::getTotal)
                .sum();

        double ingresosPasado = aprobadas.stream()
                .filter(r -> YearMonth.from(r.getFechaEvento()).equals(actual.minusMonths(1)))
                .mapToDouble(Reservacion::getTotal)
                .sum();

        int variacion = ingresosPasado == 0.0 ? 0 : (int) Math.round((ingresosMes - ingresosPasado) * 100.0 / ingresosPasado);
        double ingresosTotales = aprobadas.stream().mapToDouble(Reservacion::getTotal).sum();

        Set<LocalDate> diasOcupados = new HashSet<>();
        aprobadas.forEach(r -> {
            for (LocalDate d = r.getFechaEvento(); !d.isAfter(r.getFechaFin()); d = d.plusDays(1)) {
                if (YearMonth.from(d).equals(actual)) diasOcupados.add(d);
            }
        });
        int ocupacionMes = (int) Math.round(100.0 * diasOcupados.size() / actual.lengthOfMonth());

        List<MesIngreso> meses = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = actual.minusMonths(i);
            double tot = aprobadas.stream()
                    .filter(r -> YearMonth.from(r.getFechaEvento()).equals(ym))
                    .mapToDouble(Reservacion::getTotal)
                    .sum();
            meses.add(new MesIngreso(ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "MX")), tot));
        }

        Map<String, Integer> conteo = new HashMap<>();
        aprobadas.forEach(r -> r.getDetalles().forEach(d -> {
            conteo.put(d.getArticulo().getNombre(), conteo.getOrDefault(d.getArticulo().getNombre(), 0) + d.getCantidad());
        }));

        int totalPiezas = Math.max(1, conteo.values().stream().mapToInt(Integer::intValue).sum());
        List<TopArticulo> top = conteo.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(4)
                .map(e -> new TopArticulo(e.getKey(), e.getValue(), (int) Math.round(100.0 * e.getValue() / totalPiezas)))
                .toList();

        var recientes = todas.stream()
                .sorted(Comparator.comparing(Reservacion::getCreadoEn).reversed())
                .limit(5)
                .map(reservacionService::toDto)
                .toList();

        var entregasHoy = aprobadas.stream()
                .filter(r -> r.getFechaEvento().equals(hoy))
                .map(reservacionService::toDto)
                .toList();

        var proxima = aprobadas.stream()
                .filter(r -> !r.getFechaEvento().isBefore(hoy))
                .min(Comparator.comparing(Reservacion::getFechaEvento))
                .map(reservacionService::toDto)
                .orElse(null);

        int pendientes = (int) todas.stream().filter(r -> r.getEstado() == EstadoReservacion.PENDING).count();

        return new DashboardDto(
                pendientes, aprobadas.size(), ingresosMes, ingresosTotales,
                variacion, ocupacionMes, meses, top, recientes, entregasHoy, proxima
        );
    }
}
