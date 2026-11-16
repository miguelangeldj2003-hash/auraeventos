package com.aura.eventos.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articulos")
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(name = "stock_total", nullable = false)
    private Integer stockTotal = 1;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 50)
    private String sku;

    @Column(length = 100)
    private String dimensiones;

    @Column(length = 100)
    private String material;

    @Column(columnDefinition = "TEXT")
    private String incluye;

    @Column(name = "peso_max", length = 50)
    private String pesoMax;

    @Column(name = "en_mantenimiento", nullable = false)
    private Boolean enMantenimiento = false;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Articulo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }

    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getDimensiones() { return dimensiones; }
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getIncluye() { return incluye; }
    public void setIncluye(String incluye) { this.incluye = incluye; }

    public String getPesoMax() { return pesoMax; }
    public void setPesoMax(String pesoMax) { this.pesoMax = pesoMax; }

    public Boolean getEnMantenimiento() { return enMantenimiento; }
    public void setEnMantenimiento(Boolean enMantenimiento) { this.enMantenimiento = enMantenimiento; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
