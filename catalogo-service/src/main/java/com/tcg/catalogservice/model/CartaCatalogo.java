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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cartas_catalogo")
public class CartaCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private JuegoTcg juego;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "numero_set", nullable = false, length = 30)
    private String numeroSet;

    @Column(name = "set_nombre", nullable = false, length = 100)
    private String setNombre;

    @Column(nullable = false, length = 20)
    private String rareza;

    @Column(length = 50)
    private String tipo;

    @Column(length = 30)
    private String color;

    @Column(name = "texto_carta", columnDefinition = "text")
    private String textoCarta;

    @Column(name = "imagen_url", columnDefinition = "text")
    private String imagenUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_extra", columnDefinition = "jsonb")
    private Map<String, Object> metadataExtra = new HashMap<>();

    protected CartaCatalogo() {
    }

    public CartaCatalogo(
            JuegoTcg juego,
            String nombre,
            String numeroSet,
            String setNombre,
            String rareza,
            String tipo,
            String color,
            String textoCarta,
            String imagenUrl,
            Map<String, Object> metadataExtra) {
        this.juego = juego;
        this.nombre = nombre;
        this.numeroSet = numeroSet;
        this.setNombre = setNombre;
        this.rareza = rareza;
        this.tipo = tipo;
        this.color = color;
        this.textoCarta = textoCarta;
        this.imagenUrl = imagenUrl;
        this.metadataExtra = metadataExtra == null ? new HashMap<>() : new HashMap<>(metadataExtra);
    }

    public Long getId() { return id; }
    public JuegoTcg getJuego() { return juego; }
    public void setJuego(JuegoTcg juego) { this.juego = juego; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNumeroSet() { return numeroSet; }
    public void setNumeroSet(String numeroSet) { this.numeroSet = numeroSet; }
    public String getSetNombre() { return setNombre; }
    public void setSetNombre(String setNombre) { this.setNombre = setNombre; }
    public String getRareza() { return rareza; }
    public void setRareza(String rareza) { this.rareza = rareza; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getTextoCarta() { return textoCarta; }
    public void setTextoCarta(String textoCarta) { this.textoCarta = textoCarta; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Map<String, Object> getMetadataExtra() { return metadataExtra; }
    public void setMetadataExtra(Map<String, Object> metadataExtra) {
        this.metadataExtra = metadataExtra == null ? new HashMap<>() : new HashMap<>(metadataExtra);
    }
}
