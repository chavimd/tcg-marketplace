package com.tcg.catalogservice.model;

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

@Entity
@Table(name = "producto_sellado_catalogo")
public class ProductoSelladoCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private JuegoTcg juego;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_producto", nullable = false, length = 50)
    private String tipoProducto;

    @Column(name = "set_coleccion", length = 100)
    private String setColeccion;

    @Column(name = "precio_referencia", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioReferencia = BigDecimal.ZERO;

    @Column(name = "imagen_url", columnDefinition = "text")
    private String imagenUrl;

    protected ProductoSelladoCatalogo() {
    }

    public Long getId() { return id; }
    public JuegoTcg getJuego() { return juego; }
    public String getNombre() { return nombre; }
    public String getTipoProducto() { return tipoProducto; }
    public String getSetColeccion() { return setColeccion; }
    public BigDecimal getPrecioReferencia() { return precioReferencia; }
    public String getImagenUrl() { return imagenUrl; }
}
