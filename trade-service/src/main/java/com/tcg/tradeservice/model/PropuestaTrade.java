package com.tcg.tradeservice.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propuesta_trade")
public class PropuestaTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_emisor_id", nullable = false)
    private UsuarioRef emisor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_receptor_id", nullable = false)
    private UsuarioRef receptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPropuesta estado = EstadoPropuesta.PENDIENTE;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "propuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemTrade> items = new ArrayList<>();

    protected PropuestaTrade() {
    }

    public PropuestaTrade(UsuarioRef emisor, UsuarioRef receptor) {
        this.emisor = emisor;
        this.receptor = receptor;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public void addItem(ItemTrade item) {
        items.add(item);
        item.setPropuesta(this);
    }

    public Long getId() { return id; }
    public UsuarioRef getEmisor() { return emisor; }
    public UsuarioRef getReceptor() { return receptor; }
    public EstadoPropuesta getEstado() { return estado; }
    public void setEstado(EstadoPropuesta estado) { this.estado = estado; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ItemTrade> getItems() { return items; }
}
