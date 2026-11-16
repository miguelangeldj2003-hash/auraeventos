package com.aura.eventos.api.service;

import com.aura.eventos.api.dto.ReservacionDTOs.*;
import com.aura.eventos.api.exception.ApiException;
import com.aura.eventos.api.model.*;
import com.aura.eventos.api.pdf.NotaRemisionPdf;
import com.aura.eventos.api.repository.ArticuloRepository;
import com.aura.eventos.api.repository.ReservacionRepository;
import com.aura.eventos.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReservacionService {

    private final ReservacionRepository reservacionRepository;
    private final ArticuloRepository articuloRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoService catalogoService;

    public ReservacionService(ReservacionRepository reservacionRepository, ArticuloRepository articuloRepository, UsuarioRepository usuarioRepository, CatalogoService catalogoService) {
        this.reservacionRepository = reservacionRepository;
        this.articuloRepository = articuloRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoService = catalogoService;
    }

    @Transactional
    public ReservacionDto crearReservacion(Long usuarioId, ReservacionRequest req) {
        if (req.fechaEvento() == null || req.fechaEvento().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Fecha del evento es requerida");
        }

        LocalDate fechaInicio = LocalDate.parse(req.fechaEvento());
        LocalDate fechaFin = req.fechaFin() != null && !req.fechaFin().isBlank() ? LocalDate.parse(req.fechaFin()) : fechaInicio;

        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "La fecha del evento no puede ser anterior a hoy");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ApiException(HttpStatus.CONFLICT, "La recogida no puede ser antes de la entrega");
        }

        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (dias > 30) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La reservación no puede exceder los 30 días");
        }

        if (req.items() == null || req.items().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe agregar al menos un artículo");
        }

        Usuario cliente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Map<Long, Integer> pedidos = new LinkedHashMap<>();
        for (ItemRequest item : req.items()) {
            pedidos.put(item.articuloId(), pedidos.getOrDefault(item.articuloId(), 0) + item.cantidad());
        }

        List<ReservacionDetalle> detalles = new ArrayList<>();
        double subtotalAcum = 0.0;

        Reservacion r = new Reservacion();
        r.setCliente(cliente);
        r.setFechaEvento(fechaInicio);
        r.setFechaFin(fechaFin);
        r.setDias((int) dias);
        r.setDireccion(req.direccion() != null ? req.direccion().trim() : "");
        r.setEntorno(req.entorno() != null ? Entorno.valueOf(req.entorno()) : Entorno.INTERIOR);
        r.setNotas(req.notas());
        r.setLatitud(req.latitud());
        r.setLongitud(req.longitud());
        r.setEstado(EstadoReservacion.PENDING);
        r.setCostoFlete(0.0);

        for (Map.Entry<Long, Integer> entry : pedidos.entrySet()) {
            Long artId = entry.getKey();
            Integer cant = entry.getValue();

            Articulo a = articuloRepository.findById(artId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El artículo " + artId + " no existe"));

            if (!a.getActivo() || a.getEnMantenimiento()) {
                throw new ApiException(HttpStatus.CONFLICT, "El artículo " + a.getNombre() + " no está disponible");
            }
            if (cant > a.getStockTotal()) {
                throw new ApiException(HttpStatus.CONFLICT, "No hay suficientes piezas de " + a.getNombre() + " en el negocio");
            }
            int libres = catalogoService.calcularLibres(a, fechaInicio, fechaFin, -1L);
            if (cant > libres) {
                throw new ApiException(HttpStatus.CONFLICT, a.getNombre() + " no tiene disponibilidad en esas fechas");
            }

            double precioUnit = a.getPrecioBase();
            double subItem = precioUnit * cant;
            subtotalAcum += subItem;

            detalles.add(new ReservacionDetalle(r, a, cant, precioUnit, subItem));
        }

        r.setSubtotal(subtotalAcum);
        r.setIva(Math.round(subtotalAcum * 0.16 * 100.0) / 100.0);
        r.setTotal(Math.round((r.getSubtotal() + r.getIva() + r.getCostoFlete()) * 100.0) / 100.0);
        r.setDetalles(detalles);

        Reservacion guardada = reservacionRepository.save(r);
        guardada.setFolio(String.format("AUR-%d-%04d", LocalDate.now().getYear(), guardada.getId()));
        guardada = reservacionRepository.save(guardada);

        return toDto(guardada);
    }

    public List<ReservacionDto> listarReservaciones(Long usuarioId, boolean esAdmin, String estadoStr) {
        EstadoReservacion estado = estadoStr != null ? EstadoReservacion.valueOf(estadoStr) : null;
        List<Reservacion> lista;
        if (esAdmin) {
            lista = estado != null ? reservacionRepository.findByEstado(estado) : reservacionRepository.findAll();
        } else {
            lista = estado != null ? reservacionRepository.findByClienteIdAndEstado(usuarioId, estado)
                                   : reservacionRepository.findByClienteId(usuarioId);
        }
        return lista.stream().map(this::toDto).toList();
    }

    public ReservacionDto obtenerReservacion(Long usuarioId, boolean esAdmin, Long id) {
        Reservacion r = reservacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada"));

        if (!esAdmin && !r.getCliente().getId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada");
        }
        return toDto(r);
    }

    @Transactional
    public ReservacionDto aprobar(Long id, AprobacionRequest req) {
        if (req == null || req.costoFlete() == null || req.costoFlete() < 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Asigna el costo de flete antes de aprobar");
        }

        Reservacion r = reservacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada"));

        if (r.getEstado() != EstadoReservacion.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden aprobar solicitudes pendientes");
        }

        // Re-validar disponibilidad con bloqueo pesimista en artículos
        for (ReservacionDetalle d : r.getDetalles()) {
            Articulo a = articuloRepository.findWithLockById(d.getArticulo().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));

            int libres = catalogoService.calcularLibres(a, r.getFechaEvento(), r.getFechaFin(), r.getId());
            if (d.getCantidad() > libres) {
                throw new ApiException(HttpStatus.CONFLICT, "Ya no hay disponibilidad de " + a.getNombre() + " para esas fechas");
            }
        }

        r.setEstado(EstadoReservacion.APROBADO);
        r.setCostoFlete(req.costoFlete());
        r.setTotal(Math.round((r.getSubtotal() + r.getIva() + r.getCostoFlete()) * 100.0) / 100.0);
        r.setMotivoRechazo(null);

        return toDto(reservacionRepository.save(r));
    }

    @Transactional
    public ReservacionDto rechazar(Long id, RechazoRequest req) {
        Reservacion r = reservacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada"));

        if (r.getEstado() != EstadoReservacion.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden rechazar solicitudes pendientes");
        }

        r.setEstado(EstadoReservacion.RECHAZADO);
        r.setMotivoRechazo(req != null ? req.motivo() : null);

        return toDto(reservacionRepository.save(r));
    }

    public byte[] generarNotaPdf(Long usuarioId, boolean esAdmin, Long id) {
        Reservacion r = reservacionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada"));

        if (!esAdmin && !r.getCliente().getId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Reservación no encontrada");
        }

        if (r.getEstado() != EstadoReservacion.APROBADO) {
            throw new ApiException(HttpStatus.CONFLICT, "La nota de remisión solo está disponible para solicitudes aprobadas");
        }

        return NotaRemisionPdf.generar(r);
    }

    public ReservacionDto toDto(Reservacion r) {
        List<DetalleDto> detalles = r.getDetalles().stream()
                .map(d -> new DetalleDto(
                        d.getArticulo().getId(), d.getArticulo().getSku(), d.getArticulo().getNombre(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()
                )).toList();

        return new ReservacionDto(
                r.getId(), r.getFolio(), r.getCliente().getId(), r.getCliente().getNombre(),
                r.getCliente().getCorreo(), r.getCliente().getTelefono(),
                r.getFechaEvento().toString(), r.getFechaFin().toString(), r.getDias(),
                r.getDireccion(), r.getEntorno().name(), r.getNotas(),
                r.getLatitud(), r.getLongitud(), r.getEstado().name(), r.getMotivoRechazo(),
                r.getCreadoEn().toString(), r.getCostoFlete(), r.getSubtotal(), r.getIva(),
                r.getTotal(), detalles
        );
    }
}
