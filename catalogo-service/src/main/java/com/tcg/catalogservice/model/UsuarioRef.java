package com.tcg.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Vista de solo lectura de la tabla usuarios, propiedad de auth-service.
 * Se usa para resolver el nombre del dueño de una publicación sin llamadas HTTP.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioRef {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 20)
    private String rol;

    protected UsuarioRef() {
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
}
