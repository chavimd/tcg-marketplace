package com.tcg.catalogservice.dto;

import com.tcg.catalogservice.model.Publicacion;

import java.math.BigDecimal;
import java.time.Instant;

public record PublicacionResponse(
        Long id,
        Long usuarioId,
        String username,
        String tipoItem,
        CartaResponse carta,
        String estadoConservacion,
        BigDecimal precioUnitario,
        int stock,
        String estadoPublicacion,
        Instant createdAt) {

    public static PublicacionResponse from(Publicacion publicacion) {
        return new PublicacionResponse(
                publicacion.getId(),
                publicacion.getUsuario().getId(),
                publicacion.getUsuario().getUsername(),
                publicacion.getTipoItem().name(),
                publicacion.getCarta() == null ? null : CartaResponse.from(publicacion.getCarta()),
                publicacion.getEstadoConservacion(),
                publicacion.getPrecioUnitario(),
                publicacion.getStock(),
                publicacion.getEstadoPublicacion().name(),
                publicacion.getCreatedAt());
    }
}
