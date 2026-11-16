package com.aura.eventos.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SeccionCatalogo seccion;

    public Categoria() {}

    public Categoria(Long id, String nombre, SeccionCatalogo seccion) {
        this.id = id;
        this.nombre = nombre;
        this.seccion = seccion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public SeccionCatalogo getSeccion() { return seccion; }
    public void setSeccion(SeccionCatalogo seccion) { this.seccion = seccion; }
}
