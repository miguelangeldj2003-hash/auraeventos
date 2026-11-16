package com.aura.eventos.api.repository;

import com.aura.eventos.api.model.ReservacionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservacionDetalleRepository extends JpaRepository<ReservacionDetalle, Long> {
}
