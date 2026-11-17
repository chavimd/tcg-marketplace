package com.tcg.tradeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cartas_catalogo")
public class CartaRef {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id")
    private JuegoRef juego;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "numero_set", nullable = false, length = 30)
    private String numeroSet;

    @Column(name = "set_nombre", nullable = false, length = 100)
    private String setNombre;

    @Column(nullable = false, length = 20)
    private String rareza;

    @Column(name = "imagen_url", columnDefinition = "text")
    private String imagenUrl;

    protected CartaRef() {
    }

    public Long getId() { return id; }
    public JuegoRef getJuego() { return juego; }
    public String getNombre() { return nombre; }
    public String getNumeroSet() { return numeroSet; }
    public String getSetNombre() { return setNombre; }
    public String getRareza() { return rareza; }
    public String getImagenUrl() { return imagenUrl; }
}
