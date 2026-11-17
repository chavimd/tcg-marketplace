package com.tcg.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "publicacion_inventario")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioRef usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carta_id")
    private CartaCatalogo carta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private ProductoSelladoCatalogo producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItem tipoItem;

    @Column(name = "estado_conservacion", length = 30)
    private String estadoConservacion = "Mint";

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private EstadoPublicacion estadoPublicacion = EstadoPublicacion.DISPONIBLE;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    protected Publicacion() {
    }

    public Publicacion(
            UsuarioRef usuario,
            CartaCatalogo carta,
            BigDecimal precioUnitario,
            int stock,
            String estadoConservacion) {
        this.usuario = usuario;
        this.carta = carta;
        this.tipoItem = TipoItem.CARTA;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.estadoConservacion = estadoConservacion == null || estadoConservacion.isBlank()
                ? "Mint"
                : estadoConservacion;
        this.estadoPublicacion = EstadoPublicacion.DISPONIBLE;
    }

    public Long getId() { return id; }
    public UsuarioRef getUsuario() { return usuario; }
    public CartaCatalogo getCarta() { return carta; }
    public ProductoSelladoCatalogo getProducto() { return producto; }
    public TipoItem getTipoItem() { return tipoItem; }
    public String getEstadoConservacion() { return estadoConservacion; }
    public void setEstadoConservacion(String estadoConservacion) { this.estadoConservacion = estadoConservacion; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public EstadoPublicacion getEstadoPublicacion() { return estadoPublicacion; }
    public void setEstadoPublicacion(EstadoPublicacion estadoPublicacion) { this.estadoPublicacion = estadoPublicacion; }
    public Instant getCreatedAt() { return createdAt; }
}
