package com.tcg.tradeservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "juegos_tcg")
public class JuegoRef {

    @Id
    private Integer id;

    @Column(nullable = false, length = 50)
    private String nombre;

    protected JuegoRef() {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
