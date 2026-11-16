package com.aura.eventos.api.repository;

import com.aura.eventos.api.model.EstadoReservacion;
import com.aura.eventos.api.model.Reservacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservacionRepository extends JpaRepository<Reservacion, Long> {
    List<Reservacion> findByEstado(EstadoReservacion estado);
    List<Reservacion> findByClienteId(Long clienteId);
    List<Reservacion> findByClienteIdAndEstado(Long clienteId, EstadoReservacion estado);

    @Query("SELECT r FROM Reservacion r WHERE r.estado = 'APROBADO' AND r.id != :excluirId AND r.fechaEvento <= :hasta AND r.fechaFin >= :desde")
    List<Reservacion> findAprobadasEnRangoExcluyendo(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta, @Param("excluirId") Long excluirId);

    long countByEstado(EstadoReservacion estado);
}
