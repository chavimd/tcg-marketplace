package com.tcg.tradeservice.dto;

import com.tcg.tradeservice.model.UsuarioRef;

public record ParticipanteResponse(Long id, String username) {

    public static ParticipanteResponse from(UsuarioRef usuario) {
        return new ParticipanteResponse(usuario.getId(), usuario.getUsername());
    }
}
