package com.aura.eventos.api.controller;

import com.aura.eventos.api.dto.ReservacionDTOs.*;
import com.aura.eventos.api.security.UsuarioAutenticado;
import com.aura.eventos.api.service.ReservacionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservaciones")
public class ReservacionController {

    private final ReservacionService reservacionService;

    public ReservacionController(ReservacionService reservacionService) {
        this.reservacionService = reservacionService;
    }

    @PostMapping
    public ResponseEntity<ReservacionDto> crearReservacion(
            @AuthenticationPrincipal UsuarioAutenticado user,
            @RequestBody ReservacionRequest req
    ) {
        return ResponseEntity.ok(reservacionService.crearReservacion(user.id(), req));
    }

    @GetMapping
    public ResponseEntity<List<ReservacionDto>> listarReservaciones(
            @AuthenticationPrincipal UsuarioAutenticado user,
            @RequestParam(value = "estado", required = false) String estado
    ) {
        boolean esAdmin = user.rol().name().equals("ADMIN");
        return ResponseEntity.ok(reservacionService.listarReservaciones(user.id(), esAdmin, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservacionDto> obtenerReservacion(
            @AuthenticationPrincipal UsuarioAutenticado user,
            @PathVariable("id") Long id
    ) {
        boolean esAdmin = user.rol().name().equals("ADMIN");
        return ResponseEntity.ok(reservacionService.obtenerReservacion(user.id(), esAdmin, id));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<ReservacionDto> aprobar(
            @PathVariable("id") Long id,
            @RequestBody AprobacionRequest req
    ) {
        return ResponseEntity.ok(reservacionService.aprobar(id, req));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<ReservacionDto> rechazar(
            @PathVariable("id") Long id,
            @RequestBody RechazoRequest req
    ) {
        return ResponseEntity.ok(reservacionService.rechazar(id, req));
    }

    @GetMapping("/{id}/nota-remision")
    public ResponseEntity<byte[]> descargarNotaPdf(
            @AuthenticationPrincipal UsuarioAutenticado user,
            @PathVariable("id") Long id
    ) {
        boolean esAdmin = user.rol().name().equals("ADMIN");
        byte[] pdfBytes = reservacionService.generarNotaPdf(user.id(), esAdmin, id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nota-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
