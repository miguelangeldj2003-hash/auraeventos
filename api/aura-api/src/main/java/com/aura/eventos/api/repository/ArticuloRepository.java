package com.aura.eventos.api.repository;

import com.aura.eventos.api.model.Articulo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    List<Articulo> findByActivoTrueAndEnMantenimientoFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    Optional<Articulo> findWithLockById(@Param("id") Long id);
}
