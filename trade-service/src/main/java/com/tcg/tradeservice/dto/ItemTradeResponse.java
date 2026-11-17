package com.tcg.tradeservice.dto;

import com.tcg.tradeservice.model.CartaRef;
import com.tcg.tradeservice.model.ItemTrade;
import com.tcg.tradeservice.model.Publicacion;

import java.math.BigDecimal;

public record ItemTradeResponse(
        Long id,
        String rol,
        int cantidad,
        Long publicacionId,
        Long cartaId,
        String cartaNombre,
        String numeroSet,
        String setNombre,
        String rareza,
        String imagenUrl,
        String juego,
        BigDecimal precioUnitario,
        String estadoConservacion,
        Long usuarioId,
        String username) {

    public static ItemTradeResponse from(ItemTrade item) {
        Publicacion publicacion = item.getPublicacion();
        CartaRef carta = publicacion.getCarta();
        return new ItemTradeResponse(
                item.getId(),
                item.getRolItem().name(),
                item.getCantidad(),
                publicacion.getId(),
                carta == null ? null : carta.getId(),
                carta == null ? null : carta.getNombre(),
                carta == null ? null : carta.getNumeroSet(),
                carta == null ? null : carta.getSetNombre(),
                carta == null ? null : carta.getRareza(),
                carta == null ? null : carta.getImagenUrl(),
                carta == null || carta.getJuego() == null ? null : carta.getJuego().getNombre(),
                publicacion.getPrecioUnitario(),
                publicacion.getEstadoConservacion(),
                publicacion.getUsuario().getId(),
                publicacion.getUsuario().getUsername());
    }
}
