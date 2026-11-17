package com.tcg.tradeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vista de solo lectura de la tabla usuarios, propiedad de auth-service.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioRef {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    protected UsuarioRef() {
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}
