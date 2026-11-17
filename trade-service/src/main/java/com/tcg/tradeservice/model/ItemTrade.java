package com.tcg.tradeservice.model;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "item_trade")
public class ItemTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propuesta_id", nullable = false)
    private PropuestaTrade propuesta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_item", nullable = false, length = 20)
    private RolItem rolItem;

    @Column(nullable = false)
    private int cantidad;

    protected ItemTrade() {
    }

    public ItemTrade(Publicacion publicacion, RolItem rolItem, int cantidad) {
        this.publicacion = publicacion;
        this.rolItem = rolItem;
        this.cantidad = cantidad;
    }

    void setPropuesta(PropuestaTrade propuesta) { this.propuesta = propuesta; }

    public Long getId() { return id; }
    public PropuestaTrade getPropuesta() { return propuesta; }
    public Publicacion getPublicacion() { return publicacion; }
    public RolItem getRolItem() { return rolItem; }
    public int getCantidad() { return cantidad; }
}
