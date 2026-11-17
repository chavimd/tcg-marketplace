package com.tcg.chatservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "mensaje_chat")
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "propuesta_id", nullable = false)
    private Long propuestaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private UsuarioRef emisor;

    @Column(nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "created_at")
    private Instant createdAt;

    protected MensajeChat() {
    }

    public MensajeChat(Long propuestaId, UsuarioRef emisor, String contenido) {
        this.propuestaId = propuestaId;
        this.emisor = emisor;
        this.contenido = contenido;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Long getPropuestaId() { return propuestaId; }
    public UsuarioRef getEmisor() { return emisor; }
    public String getContenido() { return contenido; }
    public Instant getCreatedAt() { return createdAt; }
}
