package com.tcg.tradeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Publicación de inventario. trade-service la lee y actualiza el stock al aceptar un trueque.
 */
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
    private CartaRef carta;

    @Column(name = "tipo_item", nullable = false, length = 20)
    private String tipoItem;

    @Column(name = "estado_conservacion", length = 30)
    private String estadoConservacion;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private int stock;

    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private String estadoPublicacion;

    protected Publicacion() {
    }

    public Publicacion(UsuarioRef usuario, CartaRef carta, BigDecimal precioUnitario, int stock,
                       String estadoConservacion, String estadoPublicacion) {
        this.usuario = usuario;
        this.carta = carta;
        this.tipoItem = "CARTA";
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.estadoConservacion = estadoConservacion;
        this.estadoPublicacion = estadoPublicacion;
    }

    public Long getId() { return id; }
    public UsuarioRef getUsuario() { return usuario; }
    public CartaRef getCarta() { return carta; }
    public String getTipoItem() { return tipoItem; }
    public String getEstadoConservacion() { return estadoConservacion; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getEstadoPublicacion() { return estadoPublicacion; }
    public void setEstadoPublicacion(String estadoPublicacion) { this.estadoPublicacion = estadoPublicacion; }
}
