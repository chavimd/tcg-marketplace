package com.tcg.authservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Guarda únicamente el hash BCrypt.

    @Column(nullable = false, length = 20)
    private String rol = "CLIENTE"; // 'ADMIN' o 'CLIENTE'

    @Column(name = "saldo_simulado", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoSimulado = BigDecimal.ZERO;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructor vacío obligatorio para JPA
    public Usuario() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public BigDecimal getSaldoSimulado() { return saldoSimulado; }
    public void setSaldoSimulado(BigDecimal saldoSimulado) {
        this.saldoSimulado = saldoSimulado == null ? BigDecimal.ZERO : saldoSimulado;
    }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}