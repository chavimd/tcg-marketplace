package com.tcg.chatservice.dto;

public record TradeResponse(Long id, String estado, Participante emisor, Participante receptor) {

    public record Participante(Long id, String username) {
    }

    public TradeParticipants participantes() {
        if (emisor == null || receptor == null) {
            throw new IllegalStateException("Trade service returned an incomplete trade");
        }
        return new TradeParticipants(emisor.username(), receptor.username());
    }
}
