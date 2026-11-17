package com.tcg.catalogservice.dto;

import com.tcg.catalogservice.model.CartaCatalogo;

import java.util.Map;

public record CartaResponse(
        Long id,
        Integer juegoId,
        String juego,
        String nombre,
        String numeroSet,
        String setNombre,
        String rareza,
        String tipo,
        String color,
        String textoCarta,
        String imagenUrl,
        Map<String, Object> metadataExtra) {

    public static CartaResponse from(CartaCatalogo carta) {
        return new CartaResponse(
                carta.getId(),
                carta.getJuego().getId(),
                carta.getJuego().getNombre(),
                carta.getNombre(),
                carta.getNumeroSet(),
                carta.getSetNombre(),
                carta.getRareza(),
                carta.getTipo(),
                carta.getColor(),
                carta.getTextoCarta(),
                carta.getImagenUrl(),
                carta.getMetadataExtra());
    }
}
