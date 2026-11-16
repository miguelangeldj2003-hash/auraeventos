package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.CatalogoDTOs.*;
import com.aura.eventos.api.model.Articulo;
import com.aura.eventos.api.model.Categoria;
import com.aura.eventos.api.repository.ArticuloRepository;
import com.aura.eventos.api.repository.CategoriaRepository;
import com.aura.eventos.api.repository.ReservacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final ArticuloRepository articuloRepository;
    private final ReservacionRepository reservacionRepository;

    public CatalogoService(CategoriaRepository categoriaRepository, ArticuloRepository articuloRepository, ReservacionRepository reservacionRepository) {
        this.categoriaRepository = categoriaRepository;
        this.articuloRepository = articuloRepository;
        this.reservacionRepository = reservacionRepository;
    }

    public List<CategoriaDto> categorias() {
        return categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(c -> new CategoriaDto(c.getId(), c.getNombre(), c.getSeccion().name()))
                .toList();
    }

    public List<ArticuloDto> articulos(String desde, String hasta) {
        LocalDate d = desde != null ? LocalDate.parse(desde) : null;
        LocalDate h = hasta != null ? LocalDate.parse(hasta) : d;

        List<Articulo> lista = articuloRepository.findByActivoTrueAndEnMantenimientoFalse();
        return lista.stream().map(a -> {
            Integer disp = null;
            if (d != null && h != null) {
                disp = calcularLibres(a, d, h, -1L);
            }
            return toDto(a, disp);
        }).toList();
    }

    public List<String> disponibilidad(String itemsParam, String mesParam) {
        YearMonth ym = YearMonth.parse(mesParam);
        LocalDate inicioMes = ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();

        // Parse items "id:cant,id:cant"
        Map<Long, Integer> pedido = new HashMap<>();
        if (itemsParam != null && !itemsParam.isBlank()) {
            for (String part : itemsParam.split(",")) {
                String[] kv = part.split(":");
                if (kv.length == 2) {
                    try {
                        pedido.put(Long.parseLong(kv[0]), Integer.parseInt(kv[1]));
                    } catch (Exception ignored) {}
                }
            }
        }

        Set<String> ocupadas = new TreeSet<>();
        for (Map.Entry<Long, Integer> entry : pedido.entrySet()) {
            Long artId = entry.getKey();
            Integer cantReq = entry.getValue();
            Articulo a = articuloRepository.findById(artId).orElse(null);
            if (a == null) continue;

            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                LocalDate dia = ym.atDay(day);
                if (a.getEnMantenimiento() || calcularLibres(a, dia, dia, -1L) < cantReq) {
                    ocupadas.add(dia.toString());
                }
            }
        }

        return new ArrayList<>(ocupadas);
    }

    public Integer calcularLibres(Articulo a, LocalDate desde, LocalDate hasta, Long excluirReservacionId) {
        if (a.getEnMantenimiento() || !a.getActivo()) return 0;
        var reservaciones = reservacionRepository.findAprobadasEnRangoExcluyendo(desde, hasta, excluirReservacionId);
        int maxOcupado = 0;
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            final LocalDate currentDay = d;
            int ocupadasEseDia = reservaciones.stream()
                    .filter(r -> !currentDay.isBefore(r.getFechaEvento()) && !currentDay.isAfter(r.getFechaFin()))
                    .flatMap(r -> r.getDetalles().stream())
                    .filter(det -> det.getArticulo().getId().equals(a.getId()))
                    .mapToInt(det -> det.getCantidad())
                    .sum();
            maxOcupado = Math.max(maxOcupado, ocupadasEseDia);
        }
        return Math.max(0, a.getStockTotal() - maxOcupado);
    }

    public ArticuloDto toDto(Articulo a, Integer disponibles) {
        return new ArticuloDto(
                a.getId(), a.getCategoria().getId(), a.getCategoria().getNombre(), a.getCategoria().getSeccion().name(),
                a.getNombre(), a.getDescripcion(), a.getPrecioBase(), a.getStockTotal(),
                disponibles, a.getImagenUrl(), a.getActivo(),
                a.getSku(), a.getDimensiones(), a.getMaterial(), a.getIncluye(),
                a.getPesoMax(), a.getEnMantenimiento(), 0
        );
    }
}
