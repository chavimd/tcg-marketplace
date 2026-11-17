package com.tcg.catalogservice.dto;

import com.tcg.catalogservice.model.JuegoTcg;

public record JuegoResponse(Integer id, String nombre, String descripcion) {

    public static JuegoResponse from(JuegoTcg juego) {
        return new JuegoResponse(juego.getId(), juego.getNombre(), juego.getDescripcion());
    }
}
